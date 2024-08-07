package au.com.kahaara.wf.orchestration.workflow;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method may be annotated to classify it as a rule within a Workflow annotated class.
 * This allows for multiple methods as rules in the same class. Note: There must still
 * be a getInfo() however you do not need to extend WorkflowRuleInterface unless you also
 * require the run() method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WorkflowMethod {

    /**
     * The rulename as mandatory parameter for methods.
     */
    public String rulename();

}
