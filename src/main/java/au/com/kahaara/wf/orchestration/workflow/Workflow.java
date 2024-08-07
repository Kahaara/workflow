package au.com.kahaara.wf.orchestration.workflow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Orchestration annotation for workflow rules. This allows a class to be inspected at run time to decide if the
 * class is to be included in the workflow engine. It makes use of reflection to examine and instantiate such objects.
 * These objects are also given spring-boot annotations to make them beans to enable autowiring.
 * <p>It indicated that an annotated class is a "Rule" that can be called by the Orchestration Engine. A rule is a
 * single element in the workflow.</p>
 * <p>The rulename parameter is optional. Further information is available at the
 * {@link Workflow}</p>
 * @author excdsn
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
public @interface Workflow {

	/**
	 * The rulename as optional parameter. If not defined then the class name becomes the rulename
	 */
	public String rulename() default "";
	
}
