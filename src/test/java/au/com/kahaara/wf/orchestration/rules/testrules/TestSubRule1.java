package au.com.kahaara.wf.orchestration.rules.testrules;

import au.com.kahaara.wf.OrchestrationConfig;
import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.rules.DefaultRuleSetType;
import au.com.kahaara.wf.orchestration.rules.RuleInfo;
import au.com.kahaara.wf.orchestration.rules.RuleResult;
import au.com.kahaara.wf.orchestration.workflow.Workflow;
import au.com.kahaara.wf.orchestration.workflow.WorkflowRuleInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This test rule demonstrates how you can have a set of sub rules to run. These are run in their
 * own space but share the request and result data.
 *
 * @author excdsn
 *
 */
@Workflow (rulename="RuleSubRule")
public class TestSubRule1 implements WorkflowRuleInterface {

	public static final Logger log = LoggerFactory.getLogger(TestSubRule1.class);

	@Override
	public RuleInfo getInfo() {
		RuleInfo r =  new RuleInfo();
		
		// This will force a failure
		//r.addCreatedResponseData(TestRule1.class);
		
		return r;
	}

	@Override
	public RuleResult runRule(OrchestrationData request) throws RulesException {

		// This could come from an enum or database or wherever you like
		String rulesStr = "RuleReturnFALSE ? : RuleReturnTrue,  \n" +
				"RuleReturnTrue ? RuleReturnTrueAgain : END,\n " +
				"  RuleReturnTrueAgain";

		log.info("Starting orchestration for "+request.getRuleSetType());

		OrchestrationConfig oc = new OrchestrationConfig();
		oc.setRuleInfoReported(true);
		oc.setRuleInfoEnforce(false);
		OrchestrationData subRequest = new OrchestrationData(oc);
		subRequest.setRuleSetType(DefaultRuleSetType.CUSTOM);
		subRequest.setCustomRules(rulesStr);
		return request.getOrchestration().runSubRules(request, subRequest);
	}

}
