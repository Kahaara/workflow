package au.com.kahaara.wf.orchestration.rules;

import au.com.kahaara.wf.orchestration.workflow.Workflow;
import au.com.kahaara.wf.orchestration.OrchestrationData;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Contains the original rule name and the initialized object. All workflow rule
 * objects must implement the {@link Workflow}. This also means that the
 * spring-boot {@link Component} is included allowing for objects to be created
 * as beans.
 * 
 * @author Simon Haddon
 */
public class RuleType {

	/**
	 * The original rule name with no change to case according to the annotated
	 * rule name in the {@link Workflow} class.
	 */
	private String ruleName;

	/**
	 * The initialized spring bean class 
	 */
	private Object ruleClass;

	private Method method;
	
	public RuleType(String ruleName, Object o) {
		this.ruleName = ruleName;
		this.ruleClass = o;
		this.method = null;
	}

	public RuleType(String ruleName, Object o, Method method) {
		this.ruleName = ruleName;
		this.ruleClass = o;
		this.method = method;
	}

	/**
	 * @return the ruleName
	 */
	public String getRuleName() {
		return ruleName;
	}
	/**
	 * @param ruleName the ruleName to set
	 */
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	/**
	 * @return the ruleClass
	 */
	public Object getRuleClass() {
		return ruleClass;
	}

	/**
	 * @return The method to invoke.
	 */
	public Method getRuleMethod() throws NoSuchMethodException {
		if (method == null) {
			//find the default runRule and return that
			method = this.getRuleClass().getClass().getMethod("runRule", OrchestrationData.class);
		}
		return method;
	}
	/**
	 * @param ruleClass the ruleClass to set
	 */
	public void setRuleClass(Object ruleClass) {
		this.ruleClass = ruleClass;
	}

}
