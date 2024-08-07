/**
 * This is the main processing package for any orchestration activities relating to processing a request.
 * <P>The method that wants to run the orchestration should have the 
 * {@link au.com.kahaara.wf.orchestration.Orchestration} package Autowired or extended.
 * The orchestration workflow is then initiated by calling the run method with a valid
 * {@link au.com.kahaara.wf.orchestration.OrchestrationData}
 * object with the workflow type set. The orchestration data hold the workflow and the mapped data for use
 * within the orchestration.
 * <P>NB: There should be no code in the orchestration unless implementing via an interface outside </P>
 * <p>If preferred the Orchestration class can be extended rather than called directly allowing
 * for data to be selected from the database for rules </p>
 * <p>The rules are defined as per the following examples
 * <ol>
 *     <li>Rule1,Rule2,Rule3,Rule4 - In this case it flows in sequential order from Rule1 through Rule4 where
 *     the result of each rule is valid, otherwise it completes processing</li>
 *     <li>Rule1?Rule2:Rule4,Rule2,Rule3,Rule4 - Runs Rule1 and if Rule1 provides a false response then jump to
 *     Rule4, otherwise keep going through the rules. NB: Rule1 could have also been written as Rule1?:Rule4 as the
 *     workflow assumes linear travel of the workflow rules if the rule returns a valid response.</li>
 *     <li>1.Rule1?2.Rule2:4.Rule3,2.Rule2?3.Rule1:5.Rule4,3.Rule14.Rule3,5.Rule4 - Rules can loop or they can call
 *     the same rule with numbered prefixes to call the same rule without looping</li>
 * </ol></p>
 * <p>As a design pattern the
 * <a href="https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern">Chain of Responsibility</a> is the closest
 * with the chaining of commands being controlled by the Orchestration and the receiver units being the rules that
 * implemented correctly</p>
 * <p>To include a class as a workflow rule in this orchestration it must implement the
 * {@link au.com.kahaara.wf.orchestration.workflow.WorkflowRuleInterface WorkflowRuleInterface}</p> and be annoted correctly with
 * the {@link au.com.kahaara.wf.orchestration.workflow.Workflow Workflow} annotation.
 *
 * @author excdsn (Simon Haddon)
 * @since 1.0.0
 * @see java.lang.annotation.Annotation Annotation
 * @see au.com.kahaara.wf.orchestration.workflow.Workflow Workflow
 * @see au.com.kahaara.wf.orchestration.workflow.WorkflowRuleInterface WorkflowRuleInterface
 * @see au.com.kahaara.wf.orchestration.workflow.WorkflowMethod WorkflowMethod
 * @see au.com.kahaara.wf.orchestration.rules.Rule Rule
 *
 */
package au.com.kahaara.wf.orchestration;