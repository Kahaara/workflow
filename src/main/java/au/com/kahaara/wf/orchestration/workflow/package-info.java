/**
 * <p>Indicated that an annotated class is a "Rule" that can be called by the Orchestration Engine. A rule is a
 * single element in the workflow.</p>
 * <p>This annotation also automatically includes the spring-boot component annotation so including Workflow
 * annotation also means that the class can support component autowiring and other spring-boot capabilities.</p>
 * <p>For a class to be considered as a workflow rule it must also implement the
 * {@link au.com.kahaara.orchestration.workflow.WorkflowRuleInterface} interface and annotated with
 * {@link au.com.kahaara.orchestration.workflow.Workflow}.</p>
 * <P>If the element rulename is defined then the rule is considered that otherwise the
 * name of the class is considered the rule name. If the classname is being used as the rule name the trailing
 * string "Rule" is stripped off the name. i.e: RuleNameOneRule results in a rulename of RuleNameOne</p>
 * <p>e.g <pre>
 * &#64;Workflow (rulename="RuleReturnTrue")
 * public class TestRule1Rule implements WorkflowRuleInterface {
 *   &#64;verride
 *   public RuleInfo getInfo() {return new RuleInfo();}
 *   &#64;Override
 *   public RuleResult runRule(OrchestrationData request,
 *                             RulesResponseWrapper response) {
 *     return new RuleResult();
 *   }
 *   &#64;WorkflowMethod(rulename="AnotherRuleInSameMethod")
 *   public RuleResult anotherRule(OrchestrationData request,
 *                             RulesResponseWrapper response) {
 *     return new RuleResult();
 *   }
 * }
 * </pre></p>
 * <p>In the above example the rule name is defined as "RuleReturnTrue". If the rule name is not define dit would
 * translate the class name to the rule name as "TestRule1". Note that there is a second method caled anotherRule that
 * also has the {@link au.com.kahaara.orchestration.workflow.WorkflowMethod WorkflowMethod} annotation
 * allowng more methods within the same class to be created as rules.</p>
 *
 * @author excdsn
 *
 */
package au.com.kahaara.wf.orchestration.workflow;