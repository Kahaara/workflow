package au.com.kahaara.wf.orchestration.rules;

import au.com.kahaara.wf.orchestration.InfoType;
import au.com.kahaara.wf.orchestration.OrchestrationMode;
import au.com.kahaara.wf.orchestration.event.EventType;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.utils.Helper;
import au.com.kahaara.wf.orchestration.workflow.Workflow;
import au.com.kahaara.wf.orchestration.workflow.WorkflowRuleInterface;
import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.OrchestrationErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * This runs an individual rule. If there is a RuleInfo object available from a method annotated with
 * the {@link Workflow} annotation via the getInfo() method then any prerequisites are tested. These include
 * <ol>
 *     <li>From addExpectedRequestData - Test if required mapped data is present</li>
 *     <li>From addExpectedResponseData - Test that any response data required exists. This data would have been created
 *     by a previous rule</li>
 *     <li>From addModifiedResponseData - Test that any response data that requires modification exists. This data would
 *     have been created by a previous rule</li>
 *     <li>From addCreatedResponseData - Make sure any new data to be created does not exist in the response data prior
 *     to running this rule</li>
 * </ol>
 * 
 * @author excdsn
 *
 */
@Component
public class RuleRunner {
	
	public static final Logger log = LoggerFactory.getLogger(RuleRunner.class);

	private static final String TEST_SPACER  = "\n  - ";
	private static final String TEST_SPACER2 = "\n    - ";

	private static final String STR_NORUN = "Unable to run ";

	/**
	 * This method applies a single rule according to the initial list driven by the 
	 * {@link OrchestrationData} {@link Rule}
	 * 
	 * @param request The ruleSet request wrapper as created by the mapping classes
	 * @param rule The rule to run
	 * @param mode The mode to run the orchestration in. In test mode it only builds a
	 *             string defining what it would have done
     * @return A flag to indicate if this worked or not
	 * @throws RulesException If there is an exception running the rule
	 */
	public RuleResult run(OrchestrationData request, Rule rule, OrchestrationMode mode) throws RulesException {

		RuleResult result;
		WorkflowRuleInterface ruleRunner = (WorkflowRuleInterface) getRuleRunnerClass(rule);

		if (OrchestrationMode.NORMAL.equals(mode)) {
			result = buildNormalResultData(rule, ruleRunner, request);
		} else {
			// Test mode
			result = buildTestResultData(rule, ruleRunner);
		}
		return result;

	}

	/**
	 * This is the normal way to run a rule. It runs the rules, reports on any problems and keeps the
	 * orchestration data up to date.
	 *
	 * @param rule The rule information
	 * @param ruleRunner The actual rule to run
	 * @param request The orchestration data
	 * @return The rule result
	 * @throws RulesException if an unhandled exception is caught
	 */
	@SuppressWarnings({"squid:S2139"})
	private RuleResult buildNormalResultData(Rule rule, WorkflowRuleInterface ruleRunner,
	                                         OrchestrationData request) throws RulesException {

		Method method; // Use reflection for running the rule
		RuleResult result;

		try {
			method = rule.getRuleType().getRuleMethod();
		} catch (NoSuchMethodException e) {
			log.error("{} {},{} due to missing or incorrect parameters on method. {}",STR_NORUN,rule.getRuleType(), rule.getRuleName(), e);
			request.getStatus().addEvent(InfoType.ERROR, EventType.EXCEPTION, OrchestrationErrorCode.ORCH5003,
					STR_NORUN + rule.getRuleType()+", "+ rule.getRuleName() + " due to method definition problem. "+e.getMessage());
			throw new RulesException(STR_NORUN + rule.getRuleType()+ " due to method definition problem. " + e.getMessage(), e);
		}

		// TODO We need to find another way to hook this in if we do pre rule tests
		//OrchestrationDataRule.testInfoRequirements(orchestrationConfig, request, ruleRunner.getInfo(), rule.getRuleName())

		log.debug("Executing rule {}",rule.getRuleName());
		try {
			if (method == null) {
				request.setCurrentRuleInfo(ruleRunner.getInfo()); // for tracking
				result = ruleRunner.runRule(request);
			} else {
				Class c = ruleRunner.getClass();
				Method ruleInfoMethod = c.getMethod("getInfo");
				request.setCurrentRuleInfo((RuleInfo) ruleInfoMethod.invoke(ruleRunner));
				result = (RuleResult) method.invoke(ruleRunner, request);
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			Throwable e2 = e.getCause();
			log.error("{}{},{}",STR_NORUN,rule.getRuleType(), rule.getRuleName(), e2);
			String msg = getExceptionMessage(e);
			request.getStatus().addEvent(InfoType.ERROR, EventType.EXCEPTION, OrchestrationErrorCode.ORCH5003,
					STR_NORUN + rule.getRuleType()+", "+ rule.getRuleName() + ". "+msg);
			throw new RulesException(STR_NORUN + rule.getRuleType()+", "+ rule.getRuleName()+ ". " + msg, e2);
		}
		log.debug("Executed rule {} with result {}",rule.getRuleName(),result.getOk()?"pass":"fail");
		return result;
	}

	/**
	 * This is a little helper to get more information from the exception message than just a "null"
	 * message. If we only have null then we take the stack trace and turn that into a string
	 * @param e The Exception
	 * @return A message string
	 */
	private String getExceptionMessage(Exception e) {
		String msg = e.getMessage();

		if (Helper.isEmpty(msg) || "null".equalsIgnoreCase(msg)) {
			// Not a good message
			msg = e.getCause().getMessage();
		}
		if (Helper.isEmpty(msg) || "null".equalsIgnoreCase(msg)) {
			// Not a good message
			msg = Arrays.toString(e.getStackTrace());
		}
		return msg;
	}



	/**
	 * This build the result data when orchestration is in test mode
	 *
	 * @param rule The rule to report on
	 * @param ruleRunner The Rule that would have been run
	 * @return A {@link }RuleResult} showing the results of what would have occurred if every rule
	 * had returned a positive rule result.
	 */
	private RuleResult buildTestResultData(Rule rule, WorkflowRuleInterface ruleRunner) {

		RuleResult result = new RuleResult(true);

		// Testing mode OrchestrationMode.TEST
		StringBuilder sb = new StringBuilder();
		sb.append(TEST_SPACER + "Rule:").append(rule.getRuleName());
		sb.append(TEST_SPACER2 + "On pass:").append(rule.getOnTrue().getRuleName());
		sb.append(TEST_SPACER2 + "On fail:").append(rule.getOnFalse().getRuleName());

		addReqsData(sb, ruleRunner);
		result.setTestInfo(sb.toString());
		return result;
	}

	/**
	 * Add in the data testing requirements as stipulated by the getInfo() RuleInfo
	 * object created by the rule
	 *
	 * @param sb The string builder to append to.
	 * @param ruleRunner The rule itself.
	 */
	private void addReqsData(StringBuilder sb, WorkflowRuleInterface ruleRunner) {
		// Get the reqs and check if they exist
		RuleInfo reqs = ruleRunner.getInfo();
		if (reqs == null) {
			sb.append("No prerequisites provided for testing via getInfo");
		} else {
			// Add the tests
			if (reqs.mappingData.isEmpty()) {
				sb.append(TEST_SPACER + "Test for expected data in initial orchestration data");
			} else {
				sb.append(TEST_SPACER + "No tests for expected data in initial orchestration data");
			}
			for (int x = 0; x < reqs.mappingData.size(); x++) {
				String d = reqs.mappingData.get(x);
				sb.append(TEST_SPACER2).append(d);
			}

			reqs = ruleRunner.getInfo();
			if (reqs.responseData.isEmpty()) {
				sb.append(TEST_SPACER + "Test for expected data generated from previous rules");
			} else {
				sb.append(TEST_SPACER + "No tests for expected data generated from previous rules");
			}
			for (int x = 0; x < reqs.responseData.size(); x++) {
				String d = reqs.responseData.get(x);
				sb.append(TEST_SPACER2).append(d);
			}

			reqs = ruleRunner.getInfo();
			if (reqs.modifiedDataClasses.isEmpty()) {
				sb.append(TEST_SPACER + "Test for expected data generated from previous rules to be modified");
			} else {
				sb.append(TEST_SPACER + "No tests for expected data generated from previous rules to be modified");
			}
			for (int x = 0; x < reqs.modifiedDataClasses.size(); x++) {
				String d = reqs.modifiedDataClasses.get(x);
				sb.append(TEST_SPACER2).append(d);
			}

			reqs = ruleRunner.getInfo();
			if (reqs.createdDataClasses.isEmpty()) {
				sb.append(TEST_SPACER + "Test for expected data to be created");
			} else {
				sb.append(TEST_SPACER + "No tests for expected data to be created");
			}
			for (int x = 0; x < reqs.createdDataClasses.size(); x++) {
				String d = reqs.createdDataClasses.get(x);
				sb.append(TEST_SPACER2).append(d);
			}
		}
	}

	/**
	 * Get the class associated with this rule
	 * @param rule The rule as defined by {@link Rule}
	 * @return The Object that can be run that implements the {@link WorkflowRuleInterface}
	 */
	private Object getRuleRunnerClass(Rule rule) {
		return rule.getRuleType().getRuleClass();
	}

}
