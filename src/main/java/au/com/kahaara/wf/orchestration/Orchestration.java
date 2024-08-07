package au.com.kahaara.wf.orchestration;

import au.com.kahaara.wf.orchestration.event.EventRecorder;
import au.com.kahaara.wf.orchestration.event.EventRecorderInterface;
import au.com.kahaara.wf.orchestration.event.EventType;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.rules.RuleList;
import au.com.kahaara.wf.orchestration.rules.RuleResult;
import au.com.kahaara.wf.orchestration.rules.RuleSet;
import au.com.kahaara.wf.orchestration.rules.RuleSetTypeInterface;
import au.com.kahaara.wf.orchestration.workflow.WorkflowRunner;
import au.com.kahaara.wf.orchestration.cache.RuleSetCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This is the main entry point to the orchestration.
 * Prior to calling this class there should be a {@link OrchestrationData} request wrapper
 * created as part of a mapping exercise which then results in the request for 
 * this component to process.
 * <P>The main method is called {@link #run(OrchestrationData) run} and the starting point once
 * the orchestration is ready to go.
 * <P>Each rule returns a {@link RuleResult RuleResult}
 * which is then used to effect the workflow using the java true/false
 * style syntax. i.e:
 * <P>If a linear orchestration workflow where an boolean false
 * returned from a rule stops processing the workflow the the default next step
 * is END which normally is a built in rule<br/>
 * <pre>
 * VALIDATEMRZ,CHECKPACE_EMR,CHECKALERTS,ETC
 * </pre>
 * C/Java style inline if statements for conditional workflow<br/>
 * <pre>
 * ValidateMRZ,CheckPACE_EMR?CheckAlerts:CheckTRIPS,CheckAlerts,etc
 * </pre>
 *  
 * @author (excdsn) Simon Haddon
 *
 */
@Component
public class Orchestration {
	
	public static final Logger log = LoggerFactory.getLogger(Orchestration.class);

	private final WorkflowRunner rules;
	
	private final EventRecorderInterface eventRecorder;

	private final RuleList availableRules;

	private final RuleSetCacheService ruleSetService;

	private OrchestrationMode mode = OrchestrationMode.NORMAL;

	private static final String ERROR_UNABLE_CREATE_LOG_STR = "Unable to create orchestration rule set from {}. {}";
	private static final String ERROR_UNABLE_CREATE_STR = "Unable to create orchestration rule set from ";

	public Orchestration(WorkflowRunner rules, EventRecorderInterface eventRecorder, RuleList availableRules, RuleSetCacheService ruleSetService) {
		this.rules = rules;
		this.eventRecorder = eventRecorder;
		this.availableRules = availableRules;
		this.ruleSetService = ruleSetService;
	}

	/**
	 * This is the main orchestration entry point. When called run all the orchestration rules
	 * associated with the request.
	 * <P>It is expected that the mapping stage of the process creates any objects required by
	 * the orchestration and also creates the rule type or the custom rules according to the
	 * requirements of the service call.
	 * 
	 * @param orchestrationData The is the {@link OrchestrationData} request as a result of successfully
	 * performing the mapping of the service request to our internal objects
	 * 
	 * @return true if the orchestration completed without exception otherwise false.
	 */
	public boolean run (OrchestrationData orchestrationData) {

		boolean status = false;

		// Allows sub-rules to run
		orchestrationData.setOrchestration(this);
		orchestrationData.setProcessingRules(true);

		try {
			// Get rule set even if the status is not OK.
			RuleSet ruleSet = getRuleSet(orchestrationData);
			orchestrationData.setRuleSet(ruleSet);
			
			if (!InfoType.ERROR.equals(orchestrationData.getStatus().getStatus())) {
				// Everything ok to proceed
				rules.processRules(orchestrationData, mode);
			}
			status = true; // orchestration must have completed normally
			
		} catch (RulesException e) {
			log.error("Unable to process orchestration {}",e.getMessage());
			orchestrationData.getStatus().addEvent(InfoType.ERROR, EventType.RULE, OrchestrationErrorCode.ORCH1001,
					"Unable to process orchestration engine rules", e.getMessage());
			
		} finally {
			eventRecorder.recordEvents(orchestrationData);
			orchestrationData.setProcessingRules(false);
			orchestrationData.setCurrentRule(null);
		}
		
		return status;
	}

	/**
	 * Run a subset of rules that can be defined previously and available to process. The difference between this
	 * call and the {@link #run(OrchestrationData)} is following
	 * <ol>
	 *     <li>It is not in a separate transaction as it is expected to be called from a rule only</li>
	 *     <li>The orchestrationData mapped data is carried through</li>
	 *     <li>The response data is carried through</li>
	 *     <li>If any events of type error occur and are recorded in the event logger then it wil return false</li>
	 * </ol>
	 * @param request The orchestration data from the parent request
	 * @param subRequest The orchestration data from the sub request
	 */
	public RuleResult runSubRules(OrchestrationData request, OrchestrationData subRequest) throws RulesException {
		RuleSet ruleSet;
		RuleResult result = new RuleResult(true);
		try {
			ruleSet = this.getRuleSet(subRequest);
			subRequest.setRuleSet(ruleSet);
			subRequest.setRequestData(request.getRequestData());
			subRequest.setResultData(request.getResultData());
			subRequest.setOrchestrationConfig(request.getOrchestrationConfig());
			subRequest.setOrchestration(request.getOrchestration());
			result = rules.runRuleSet(subRequest, mode, false);

		} catch (RulesException e) {
			log.error("Unable to process orchestration {}",e.getMessage());
			request.getStatus().addEvent(InfoType.ERROR, EventType.RULE, OrchestrationErrorCode.ORCH1001,
					"Unable to process orchestration engine sub rules", e.getMessage());
			throw new RulesException("Unable to process orchestration engine sub rules from "
					+request.getCurrentRule()+ ". " + e.getMessage());
		}
		return result;
	}

	/**
	 * Get the orchestration rules from either the cache or, if no cache
	 * entry then use the default ruleset from {@link RuleSetTypeInterface} that applies
	 * to this request.
	 *
	 * @param request The rule request wrapper that contains the orchestration 
	 * {@link RuleSetTypeInterface}
	 * @return The {@link RuleSet}
	 * @throws RulesException If it unable to get the orchestration workflow rules
	 */
	private RuleSet getRuleSet(OrchestrationData request) throws RulesException {
		
		String ruleStr;
		RuleSet ruleSet;

		RuleSetTypeInterface r = request.getRuleSetType();

		if (r.getEnumName().equalsIgnoreCase("CUSTOM")
				|| (request.getCustomRules() != null && !request.getCustomRules().isEmpty())) {
			ruleStr = request.getCustomRules();
		} else {
			// Get from pre defined list
			ruleStr = request.getRuleSetType().getRules();
		}
			
		// Create the ruleset from the orchestration workflow string provided
		try {
			if (ruleSetService != null) {
				ruleSet = ruleSetService.getRuleSet(ruleStr);
			} else {
				ruleSet = new RuleSet(ruleStr, availableRules);
			}
		} catch (RulesException e) {
			log.error(ERROR_UNABLE_CREATE_LOG_STR,ruleStr,e.getMessage());
			throw new RulesException(ERROR_UNABLE_CREATE_STR+ruleStr+". "+e.getMessage());
		} catch (Exception e) {
			log.error(ERROR_UNABLE_CREATE_LOG_STR,ruleStr,e);
			throw new RulesException(ERROR_UNABLE_CREATE_STR+ruleStr, e);
		}
		
		return ruleSet;
	}

	/**
	 * Get the mode the orchestration is running in
	 *
	 * @return The {@link OrchestrationMode}
	 */
	public OrchestrationMode getMode() {
		return mode;
	}

	/**
	 * Set the orchestration mode for this run.
	 *
	 * @param mode The {@link OrchestrationMode}
	 */
	public void setMode(OrchestrationMode mode) {
		this.mode = mode;
	}

}
