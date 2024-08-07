/**
 * 
 */
package au.com.kahaara.wf.orchestration.rules.testrules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.rules.RuleInfo;
import au.com.kahaara.wf.orchestration.rules.RuleResult;
import au.com.kahaara.wf.orchestration.workflow.Workflow;
import au.com.kahaara.wf.orchestration.workflow.WorkflowRuleInterface;

/**
 * @author excdsn
 *
 */
@Workflow (rulename="RuleReturnTrueAgain")
public class TestRule3 implements WorkflowRuleInterface {

	public static final Logger log = LoggerFactory.getLogger(TestRule3.class);
	
	@Override
	public RuleInfo getInfo() {
		return new RuleInfo();
	}

	@Override
	public RuleResult runRule(OrchestrationData request) {
		log.info("Starting orchestration for "+request.getRuleSetType());
		RuleResult retVal = new RuleResult(true);
		return retVal ;
	}

}
