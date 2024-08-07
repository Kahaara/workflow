package au.com.kahaara.wf.orchestration.workflow;

import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.rules.RuleInfo;
import au.com.kahaara.wf.orchestration.rules.RuleResult;

/**
 * This interface must be implemented for a workflow rule to be considered for the 
 * orchestration workflow rules.
 * <P>Each class may only have 1 rule and must also add the {@link Workflow} annotation
 * if only using this interface. If you want more than one rule in a class then extend it using
 * the {@link WorkflowMethod} interface</P>
 * @see WorkflowMethod
 * @author excdsn
 *
 */
public interface WorkflowRuleInterface {
		
	/**
	 * Return information about what this rule requires in the way of data to run correctly. 
	 * This is then interrogated prior to running the rule to ensure the objects exist. If they 
	 * don't then the orchestration is stopped with an appropriate error raised.
	 * <P>When the {@link OrchestrationData} is accessed via either a getData or putData the intent
	 * must be defined via the {@link RuleInfo} first or access to the objects store is not permitted and an exception is thrown.
	 * The exception is realised during the {@link #runRule(OrchestrationData)} stage of the workflow</P>
	 * @see RuleInfo
	 * @return The RuleInfo object
	 */
	public RuleInfo getInfo();
	
	/**
	 * Privids the common interface to access and run the rule when it is due to be accesses
	 * according to the defined workflow. It runs the rule with the appropriate data provided
	 * via {@link OrchestrationData} object.
	 *
	 * <P><strong>At no time shold this method ever be run directly. It is only used by the orchestration
	 * and if called directly then it will break the workflow</strong></P>
	 * 
	 * @param orchestrationData This is the mapped request data as a result of the mapping that
	 * would have been called prior to the orchestration starting.
	 * <P> Stores any data throughout the orchestration workflow rules. This
	 * data gets created and modified by the orchestration rules. The data may be JPA
	 * objects or POJO objects but only one object of each type may be stored.</P>
	 * @return The rule processing result.
	 * @exception RulesException for capturing any exception from a rule.
	 */
	public RuleResult runRule(OrchestrationData orchestrationData) throws RulesException;

}
