package au.com.kahaara.wf.orchestration.rules;

/**
 * A rule is a RuleType. Each rule is a stage in the workflow that can return
 * a true or false result from processing the request. On return then the true 
 * or false condition will be interrogated to see if there is workflow decision
 * point that can fork off to a different set of ruleSet.
 * <P>For ease of use it is strongly suggested that the conditional ruleSet
 * are not used yet till we get a better understanding of how they will be configured
 * <P>We use a C/Java inline condition statement structure
 * <p>e.g: <code>WorkflowRule1?ruleOnTrue:ruleOnFalse, RuleOnTrue?nextRule, RuleOnFalse,NextRule,etc</code></p>
 * <p>Also, rules can be prefixed with a sequence number to assist in defining the same rule
 * many times and thus allowing the workflow control to select the correct rule if multiple
 * definitions</p>
 *  e.g: <code>WorkflowRule?RuleOnTrue:RuleOnFalse, 1.RuleOnTrue?2.RuleOnTrue, RuleOnFalse,2.RuleOnTrue,1.NextRule, etc</code>
 * 
 * @author excdsn
 *
 */
public class Rule {

	/**
	 * This is the original rulename as defined in the rule list string for this particular
	 * set of rules. NB: This can be different to the rule name defined in the RuleType object
	 */
	private String ruleName;
	/**
	 * This is the main rule workflow item to follow
	 */
	private RuleType ruleType;
	/**
	 * The true condition to follow if set
	 */
	private Rule onTrue = null;
	/**
	 * The false condition to follow if set
	 */
	private Rule onFalse = null;
	
	public Rule(RuleType crt) {
		this.ruleType = crt;
		this.ruleName = crt.getRuleName();
	}

	Rule(RuleType crt, String ruleName) {
		this.ruleType = crt;
		this.ruleName = ruleName;
	}

	/**
	 * Get the original rule name
	 *
	 * @return The rule name in it's original form
	 */
	public String getRuleName() {
		return ruleName;
	}

	/**
	 * Set the rule name in it's original form
	 *
	 * @param ruleName The original rule name
	 */
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	/**
	 * @return the ruleType
	 */
	public RuleType getRuleType() {
		return ruleType;
	}
	/**
	 * @param ruleType the ruleType to set
	 */
	void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}
	/**
	 * @return the onTrue
	 */
	public Rule getOnTrue() {
		return onTrue;
	}
	/**
	 * @param onTrue the onTrue to set
	 */
	void setOnTrue(Rule onTrue) {
		this.onTrue = onTrue;
	}
	/**
	 * @return the onFalse
	 */
	public Rule getOnFalse() {
		return onFalse;
	}
	/**
	 * @param onFalse the onFalse to set
	 */
	void setOnFalse(Rule onFalse) {
		this.onFalse = onFalse;
	}
	
	@Override
	public String toString() {
		String onTrueStr = this.onTrue != null ? this.onTrue.getRuleName() : "null";
		String onFalseStr = this.onFalse != null ? this.onFalse.getRuleName() : "null";

		return "[rule:"+this.ruleName+",pass:"+onTrueStr+",fail:"+onFalseStr+"]";
	}

}
