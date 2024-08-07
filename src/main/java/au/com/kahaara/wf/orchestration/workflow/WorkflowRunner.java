package au.com.kahaara.wf.orchestration.workflow;

import au.com.kahaara.wf.OrchestrationConfig;
import au.com.kahaara.wf.orchestration.OrchestrationMode;
import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.rules.*;
import au.com.kahaara.wf.orchestration.rules.def.WorkflowEnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;


/**
 * This is the main entry point to processing a rule set.
 * 
 * @author excdsn
 *
 */
@Service
public class WorkflowRunner {
	
	public static final Logger log = LoggerFactory.getLogger(WorkflowRunner.class);

	private static final int RULE_MAX_RUN = 500;

	private final RuleRunner runner;
	
	public WorkflowRunner(RuleRunner runner) {

		this.runner = runner;
	}
	
	/**
	 * Apply the appropriate ruleSet to the to result set. The ruleSet can be a predetermined set 
	 * of ruleSet {@link RuleSetTypeInterface} or a custom list of ruleSet {@link RuleType}. The rule to apply
	 * is dependent on the entry on the rule profile.
	 * 
	 * @throws RulesException If there is an error processing rules
	 */
	@Transactional
	public RuleResult processRules(OrchestrationData request, OrchestrationMode mode) throws RulesException{
		
		RuleResult ruleResult = runRuleSet(request, mode, true);
		log.debug("Success status from running rules are {}",ruleResult.getOk());
		return ruleResult;

	}

	/**
	 * Run the rule set. If this is a sub workflow then don't run the start.end rules
	 *
	 * @param request The request data
	 * @param mode The {@link OrchestrationMode}
	 * @param topLevelRunner Is this the top level workflow. If so then also run start,end otherwise done
	 * @return A rule result from the last rule run
	 * @throws RulesException If there is a rule processing exception
	 */
	public RuleResult runRuleSet(OrchestrationData request, OrchestrationMode mode,
	                             boolean topLevelRunner) throws RulesException {

		int ruleCount = 0; // Sanity check
		boolean hasMoreRules = true;
		List<Rule> ruleSet = request.getRuleSet().getRules();
		RuleResult result = new RuleResult(true);

		log.debug("applyRules: Processing profile rule set {}",ruleSet);

		// Handles conditions
		if (!ruleSet.isEmpty()) {
			Rule rule = ruleSet.get(0);
			while (hasMoreRules) {
				if (rule.getRuleType().getRuleName().equals(OrchestrationConfig.WORKFLOW_END)) {
					// The end but still run the end rule
					hasMoreRules = false;
					if (!topLevelRunner) {
						// Get straight out as we don't run the END rule
						break;
					}
				} else if (!topLevelRunner && rule.getRuleType().getRuleName().equals(OrchestrationConfig.WORKFLOW_START)) {
					// Skip the start as not top level
					rule = getRule(rule.getOnTrue().getRuleType(), request.getRuleSet().getRules());
				}

				// Go ahead and run the rule
				result = runRule(request, rule, mode);
				if (result.isOk()) {
					rule = rule.getOnTrue();
				} else {
					rule = rule.getOnFalse();
				}

				// Sanity check
				if (ruleCount++ >= RULE_MAX_RUN && hasMoreRules) {
					throw new RulesException("Limit of "+ RULE_MAX_RUN +" reached while running rules");
				}
			}
		}
		return result;

	}

	private RuleResult runRule(OrchestrationData request, Rule rule, OrchestrationMode mode) throws RulesException {

		request.setCurrentRule(rule);
		RuleResult result = runner.run(request, rule, mode);
		if (OrchestrationMode.TEST.equals(mode)) {
			log.debug(result.getTestInfo());
			request.getTestResults().add(result.getTestInfo());
		}
		return result;
	}

	private Rule getRule(RuleType rule, List<Rule> ruleSet) throws RulesException {
		Optional<Rule> r = ruleSet.stream().filter(p -> p.getRuleType().equals(rule)).findFirst();
		
		if (r.isPresent()) {
			return r.get();
		}

		//Can't find the rule so get the END
		r = ruleSet.stream().filter(p -> p.getRuleType().getRuleName().equals(OrchestrationConfig.WORKFLOW_END)).findFirst();
		if (r.isPresent()) {
			return r.get();
		}

		// Can't find END so create a default END
		try {
			Method method = WorkflowEnd.class.getMethod("runRule", OrchestrationData.class);
			RuleType newRule = new RuleType(OrchestrationConfig.WORKFLOW_END, new WorkflowEnd(), method);
			return new Rule(newRule);
		} catch (NoSuchMethodException e) {
			throw new RulesException("Unable to locate default workflow end class invoke method",e);
		}
	}

}
