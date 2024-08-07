package au.com.kahaara.wf.orchestration.rules.def;

import au.com.kahaara.wf.OrchestrationConfig;
import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.workflow.Workflow;
import au.com.kahaara.wf.orchestration.rules.RuleInfo;
import au.com.kahaara.wf.orchestration.rules.RuleResult;
import au.com.kahaara.wf.orchestration.workflow.WorkflowRuleInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default "END" rule that is applied to a workflow
 * This class can be overridden with one in the applicatio if specific actions are
 * required.
 *
 * @author excdsn
 *
 */
@Workflow(rulename= OrchestrationConfig.WORKFLOW_END)
public class WorkflowEnd implements WorkflowRuleInterface {

	public static final Logger log = LoggerFactory.getLogger(WorkflowEnd.class);
	
	@Override
	public RuleInfo getInfo() {
		return new RuleInfo();
	}

	@Override
	public RuleResult runRule(OrchestrationData request) {
		log.info("Ending orchestration for "+request.getRuleSetType());
		RuleResult retVal = new RuleResult(false);
		return retVal ;
	}

}
