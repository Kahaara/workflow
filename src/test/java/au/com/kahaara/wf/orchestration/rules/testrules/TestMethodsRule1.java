/**
 * 
 */
package au.com.kahaara.wf.orchestration.rules.testrules;

import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.rules.RuleInfo;
import au.com.kahaara.wf.orchestration.rules.RuleResult;
import au.com.kahaara.wf.orchestration.workflow.Workflow;
import au.com.kahaara.wf.orchestration.workflow.WorkflowMethod;
import au.com.kahaara.wf.orchestration.workflow.WorkflowRuleInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author excdsn
 *
 */
@Workflow
public class TestMethodsRule1 implements WorkflowRuleInterface {

	public static final Logger log = LoggerFactory.getLogger(TestMethodsRule1.class);
	
	@Override
	public RuleInfo getInfo() {
		RuleInfo r =  new RuleInfo();
		
		// This will force a failure
		//r.addCreatedResponseData(TestRule1.class);
		
		return r;
	}

	@Override
	public RuleResult runRule(OrchestrationData request) {
		log.info("Starting orchestration for "+request.getRuleSetType());
		RuleResult retVal = new RuleResult(true);
		return retVal ;
	}

	@WorkflowMethod(rulename="methodRule1")
	public RuleResult methodRule1(OrchestrationData request) {
		log.info("Starting orchestration for "+request.getRuleSetType());
		RuleResult retVal = new RuleResult(true);
		return retVal ;
	}

}
