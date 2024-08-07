package au.com.kahaara.wf.orchestration.rules;

/**
 * This interface must be implemented by any enum classes that are defining their own rule sets. Each
 * enum type must be a name with the rules, if any, defined as a single parameter string of rules following
 * the orchestration requirements for rule definitions. If CUSTOM is used then it is expected that a string is
 * provided in the orchestration data that describes the rules.
 *
 * @author excdsn
 *
 */
public interface RuleSetTypeInterface {

	/**
	 * Get a csv string of ruleSet in the order they are to be processed
	 *
	 * @return A csv string
	 */
	public String getRules();

	/**
	 * Return the enum name as a string
	 *
	 * @return The enum name as a string
	 */
	public String getEnumName();

}
