package au.com.kahaara.wf.orchestration.rules;

/**
 * What ruleset to be used in determining the outcome of a probable match result. A ruleset is
 * a pre-defined order of events or sub ruleSet that are followed in order to produce the required 
 * outcome.
 * See 
 * <ul>
 * 	<li>{@link #NONE}
 * 	<li>{@link #CUSTOM}
 * 	<li>{@link #UNKNOWN} 
 * </ul>
 * 
 * @author Simon Haddon
 *
 */
public enum DefaultRuleSetType implements RuleSetTypeInterface {

	/**
	 * No ruleSet to be applied. 
	 *  
	 * <p>Rules : 
	 * N/A
	 */
	NONE(""),
	CUSTOM(""), 	
	UNKNOWN("");

	private final String value;

	DefaultRuleSetType(String value) {
		this.value = value;
	}

	@Override
	public String getRules() {
		return String.valueOf(value);
	}

	@Override
	public String getEnumName() {
		return this.name();
	}


}
