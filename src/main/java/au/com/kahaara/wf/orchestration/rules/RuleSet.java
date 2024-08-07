package au.com.kahaara.wf.orchestration.rules;

import au.com.kahaara.wf.OrchestrationConfig;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Creates and then contains a valid set of ruleSet for orchestration. The rule set is the orchestration workflow
 * and is defined in either the database or in the {@link RuleSetTypeInterface} as defined in the main application
 * that calls this class.
 * <p>Create a set of ruleSet from the 2 arrays created by the split method.</p>
 * Throws Exception If a rule is defined in the string but not defined in the allowable
 * rule sets. If the rule set doesn't have a START and and END these will be automatically
 * added.
 * <P> For example
 *  <pre>
 *  START,VALIDATEMRZ,CHECKPACE_EMR?1.CHECKALERTS:CHECKTRIPS,1.CHECKALERTS,END
 *  </pre>
 *  <and>
 *  <pre>
 *  VALIDATEMRZ,CHECKPACE_EMR?CHECKALERTS:CHECKTRIPS,CHECKALERTS
 *  </pre>
 *  <p>If a rule needs to be repeated but not as a result of a loop in the workflow then
 *  the repeated rules may be prefixed with a number followed by an underscore "." then the workflow
 *  rule name</p>
 * 
 * @author (excdsn) Simon Haddon
 *
 */
public class RuleSet {

	public static final Logger log = LoggerFactory.getLogger(RuleSet.class);

	private static final int RULE = 0;
	private static final int ONTRUE = 1;
	private static final int ONFALSE = 2;


	/**
	 * The rule list string converted into an array of rule objects.
	 */
	private List<Rule> mainRules = new ArrayList<>();

	/**
	 * The original string representation of the rules
	 */
	private String[] ruleList = null;

	/**
	 * Entry point with nothing initialized
	 */
	public RuleSet() {
	}

	/**
	 * Creates the rule set based on the provided rule list
	 *
	 * @param ruleset The ruleset
	 * @param ruleList The list of rules available to use
	 * @throws RulesException if there is an exception
	 */
	public RuleSet(DefaultRuleSetType ruleset, RuleList ruleList) throws RulesException {
		String rules = ruleset.getRules();
		create(rules, ruleList.getRuleList());
	}

	/**
	 * Creates the rule set based on the provided rule list
	 *
	 * @param rules The string list of rules to use as the rule set
	 * @param ruleList The list of rules available for use
	 * @throws RulesException If there is an exception thrown
	 */
	public RuleSet(String rules, RuleList ruleList) throws RulesException {
		create(rules, ruleList.getRuleList());
	}

	public RuleSet(String rules, Map<String, RuleType> availableRules) throws RulesException {
		create(rules, availableRules);
	}

	public void setRules(String rules, Map<String, RuleType> availableRules) throws RulesException {
		create(rules, availableRules);
	}
	
	public void setRules(String rules, RuleList ruleList) throws RulesException {
		create(rules, ruleList.getRuleList());
	}
	
	/**
	 * Create a set of ruleSet from the string list of rules provided.
	 * @param rules THe string list of rules
	 * @param availableRules a list of available rules created by the {@link RuleList} component
	 * on startup
	 * @throws RulesException If a rule is defined in the string but not defined in the allowable
	 */
	private void create(String rules, Map<String, RuleType> availableRules) throws RulesException {

		log.info("Creating rule set from {}",rules);
		this.mainRules = new ArrayList<>();
		
		if (rules == null) {
			throw new RulesException("Provided ruleSet are null. Value must be present");
		} else {
			ruleList = createRuleList(rules);
		}

		// Test the rules
		for (String r : ruleList) {
			try {
				if (!r.isEmpty()) {
					String nextR = getNextRuleInList(r, ruleList);
					String[] splitRule = conditionalSplit(r, nextR);
					if (getRuleNameInRuleListCount(splitRule[RULE], ruleList) > 1) {
						log.error("Cannot have the same rule defined more than once in a rule set {}",splitRule[RULE]);
						throw new RulesException("Cannot have the same rule defined more than once in a rule set. Following rule is incorrect: "+splitRule[RULE]);
					}
				}
			} catch (IllegalArgumentException e) {
				throw new RulesException("Rule type "+r+" is not defined in allowable rule types. Please fix profile configuration",e);
			}
		}

		// Create the rules
		createMainRules(ruleList, availableRules);
		createMainRuleConditions(ruleList);

		log.debug("Created rule set {}",this.mainRules);


	}

	/**
	 * Get the next sequenced rule in the list
	 * @param r The rule name
	 * @param ruleList The rule list
	 * @return The rule name
	 */
	private String getNextRuleInList(String r, String[] ruleList) {

		String nextR = OrchestrationConfig.WORKFLOW_END;
		if (r != null && !r.equalsIgnoreCase(OrchestrationConfig.WORKFLOW_END)) {
			int x = 0;
			String[] s1 = r.split("\\?");
			for (String n : ruleList) {
				String[] s2 = n.split("\\?");
				if (s2[0].equalsIgnoreCase(s1[0])) {
					// Get the next one. We can't run afoul of the boundary check since
					// there is always an END at the end
					nextR = ruleList[++x];
					break;
				}
				x++;
			}

			// Get the first part of the name without the conditions
			String[] s = nextR.split("\\?");
			nextR = s[0];
		}

		return nextR;


	}

	/**
	 * Test the number of times this rule is in the lst
	 *
	 * @param r The rule name
	 * @param ruleList The rule list
	 * @return The count
	 */
	private int getRuleNameInRuleListCount(String r, String[] ruleList) {
		int count = 0;
		String[] s1 = r.split("\\?");
		for (String n : ruleList) {
			String[] s2 = n.split("\\?");
			if (s2[0].equalsIgnoreCase(s1[0])) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Create all the main rules with no conditional actions yet
	 *
	 * @param ruleList The list of rules
	 * @param availableRules The list of available mapped classes
	 */
	private void createMainRules(String[] ruleList, Map<String, RuleType> availableRules) throws RulesException {

		log.trace("Creating main rule list");
		for (String n : ruleList) {
			String[] splitRule = n.split("\\?"); // Just need to first part
			// See if the rule is defined in the list already
			Rule rule = getRule(splitRule[RULE], mainRules);
			if (rule != null) {
				// Woopsie
				log.error("Cannot have the same rule defined more than once in a rule set {}",splitRule[RULE]);
				throw new RulesException("Cannot have the same rule defined more than once in a rule set. Following rule is incorrect: "+splitRule[RULE]);
			} else {
				RuleType crt = getRuleType(splitRule[RULE], availableRules);
				rule = new Rule(crt, splitRule[RULE]);
				mainRules.add(rule);
			}
			log.trace("Created rule {}",splitRule[RULE]);

		}

	}

	/**
	 * Go through the list and attach the conditios to their rules
	 *
	 * @param ruleList The list of rules
	 */
	private void createMainRuleConditions(String[] ruleList) throws RulesException {

		log.trace("Creating conditional rules for main rules");
		for (String r : ruleList) {
			String nextR = getNextRuleInList(r, ruleList);
			String[] splitRule = conditionalSplit(r, nextR);

			Rule rule = getRule(splitRule[RULE], mainRules);
			if (rule == null) {
				throw new RulesException("Cannot find rule when should have been defined. "+r);
			}

			// ONTRUE
			Rule trueRule = getRule(splitRule[ONTRUE], mainRules);
			if (trueRule == null) {
				log.error("Cannot locate true condition {} rule defined as {}",splitRule[ONTRUE],r);
				throw new RulesException("Cannot locate true condition rule in main list. Following rule is incorrect: "+r);
			}
			rule.setOnTrue(trueRule);

			// ONFALSE
			Rule falseRule = getRule(splitRule[ONFALSE], mainRules);
			if (falseRule == null) {
				log.error("Cannot locate false condition {} rule defined as {}",splitRule[ONFALSE],r);
				throw new RulesException("Cannot locate true condition rule in main list. Following rule is incorrect: "+r);
			}
			rule.setOnFalse(falseRule);
			log.trace("Created conditions for rule {} {}", splitRule[RULE], rule);

		}
	}


	/**
	 * Wrap the rule list string with and start end rule if required then split into a string
	 * array.
	 *
	 * @param rules The rules represented as a string
	 * @return The String[] array
	 */
	private String[] createRuleList(String rules) {
		if (!rules.toUpperCase().startsWith(OrchestrationConfig.WORKFLOW_START+",")) {
			rules = OrchestrationConfig.WORKFLOW_START+","+rules;
		}
		if (!rules.toUpperCase().endsWith(","+OrchestrationConfig.WORKFLOW_END)) {
			rules = rules+","+OrchestrationConfig.WORKFLOW_END;
		}

		// Clean the string
		rules = rules.replace(",,", ",");  // Remove duplicate commas
		rules = rules.replace(" ","");     // No spaces
		rules = rules.replaceAll("["+System.lineSeparator()+"]",""); // Any lf or cr

		String[] splitRuleList = rules.split(",");
		log.trace("Rule list {}", Arrays.stream(splitRuleList).toArray());
		return splitRuleList;
	}

	private Rule getRule(String string, List<Rule> mainRules) {
		for (Rule r : mainRules) {
			if (r.getRuleName().equalsIgnoreCase(string)) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Given a rule name then find the rule that it relates to. This allows for rules to
	 * be prefixed with a number i.e: 1.TheRuleName.
	 *
	 * @param ruleName The rulename
	 * @param availableRules The mapped list of available rules
	 * @return Returns the rule type
	 * @throws RulesException Throws a rule exception if rule not found
	 */
	private RuleType getRuleType(String ruleName, Map<String, RuleType> availableRules) throws RulesException {

		String rn = ruleName.replaceAll("[0-9].", "");

		if (availableRules.containsKey(rn.toUpperCase())) {
			return availableRules.get(rn.toUpperCase());
		}
		
		// If we got here we're stuffed
		throw new RulesException("Missing rule "+ruleName);
		
	}

	/**
	 * Split a rule up to the "C" style inline if statements structure. If the true "?" condition is
	 * provided then that becomes the next workflow rule otherwise the next is used. If the false ":"
	 * condition is supplied then that is set as the rule on a false condition otherwise END.
	 * 
	 * @param rule The rule to evaluate
	 * @param nextRule The default next rule in the workflow
	 * @return a string array of 3 items.
	 */
	private String[] conditionalSplit(String rule, String nextRule) {
		
		String[] ruleStr = new String[3];
		String defaultActionOnFail=OrchestrationConfig.WORKFLOW_END;

		if (rule.contains("?")) {
			String[] tempStr = rule.split("[?:]"); // Get conditions
			ruleStr[RULE] = tempStr[RULE];
			ruleStr[ONTRUE] = rule.contains("?") && !tempStr[ONTRUE].isEmpty() ? tempStr[ONTRUE] : nextRule;
			ruleStr[ONFALSE] = rule.contains(":") && !tempStr[ONFALSE].isEmpty() ? tempStr[ONFALSE] : defaultActionOnFail;
			
		} else {
			ruleStr[RULE] = rule;
			ruleStr[ONTRUE] = nextRule;
			// If a false is returned and no conditional is applied then
			// we go directly to the end of processing
			ruleStr[ONFALSE] = defaultActionOnFail;
		}
		
		return ruleStr;
		
	}

	/**
	 * @return the mainRules
	 */
	public List<Rule> getRules() {
		return mainRules;
	}

	/**
	 * @param mainRules the mainRules to set
	 */
	public void setRules(List<Rule> mainRules) {
		this.mainRules = mainRules;
	}

	public String[] getRuleList() {
		return ruleList;
	}

	public void setRuleList(String[] ruleList) {
		this.ruleList = ruleList;
	}

	@Override
	public String toString() {
		return mainRules.toString();
	}
	
}

