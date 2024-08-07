/**
 * 
 */
package au.com.kahaara.wf.orchestration.rules.testrules;

import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.rules.RuleInfo;
import au.com.kahaara.wf.orchestration.rules.RuleResult;
import au.com.kahaara.wf.orchestration.workflow.Workflow;
import au.com.kahaara.wf.orchestration.workflow.WorkflowRuleInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author excdsn
 *
 */
@Workflow (rulename="RuleReturnFalse")
public class TestRule2 implements WorkflowRuleInterface {

	public static final Logger log = LoggerFactory.getLogger(TestRule2.class);
	
	@Override
	public RuleInfo getInfo() {
		return new RuleInfo();
	}

	@Override
	public RuleResult runRule(OrchestrationData request) {
		log.info("Starting orchestration for "+request.getRuleSetType());
		RuleResult retVal = new RuleResult(false);
		return retVal ;
	}

}
