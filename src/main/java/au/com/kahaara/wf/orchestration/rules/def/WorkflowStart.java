package au.com.kahaara.wf.orchestration.rules.def;

import au.com.kahaara.wf.OrchestrationConfig;
import au.com.kahaara.wf.orchestration.rules.RuleInfo;
import au.com.kahaara.wf.orchestration.workflow.Workflow;
import au.com.kahaara.wf.orchestration.workflow.WorkflowRuleInterface;
import au.com.kahaara.wf.orchestration.rules.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import au.com.kahaara.wf.orchestration.OrchestrationData;

/**
 * This is the default "START" rule that is applied to a workflow.
 * This class can be overridden with one in the applicatio if specific actions are
 * required.
 *
 * @author excdsn
 *
 */
@Workflow(rulename= OrchestrationConfig.WORKFLOW_START)
public class WorkflowStart implements WorkflowRuleInterface {

	public static final Logger log = LoggerFactory.getLogger(WorkflowStart.class);
	
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
