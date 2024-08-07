package au.com.kahaara.wf.orchestration.rules;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import au.com.kahaara.wf.OrchestrationConfig;
import au.com.kahaara.wf.orchestration.rules.def.WorkflowEnd;
import au.com.kahaara.wf.orchestration.rules.def.WorkflowStart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.rules.testrules.TestRule1;
import au.com.kahaara.wf.orchestration.rules.testrules.TestRule2;
import au.com.kahaara.wf.orchestration.rules.testrules.TestRule3;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.env.MockEnvironment;

/**
 * @author excdsn
 *
 */
class RuleSetTest {

	static String rulesNumbers = "1.RuleReturnFALSE?2.RuleReturnTrue,2.RuleReturnTrue?3.RuleReturnTrueAgain:END,3.RuleReturnTrueAgain";
	static String rulesNoNumbers = "RuleReturnFALSE?RuleReturnTrue,RuleReturnTrue?RuleReturnTrueAgain:END,RuleReturnTrueAgain";
	static String rulesDuplicate = "RuleReturnFALSE?:RuleReturnTrue,RuleReturnTrue?RuleReturnFALSE:END,RuleReturnFALSE";
	static String rulesMissing = "RuleReturnFALSE?:RuleReturnTrue,RuleReturnTrue?RuleReturnTrueMaybe:END,RuleReturnTrueAgain";

	static OrchestrationData request;
	
	static RuleList ruleList;

	@Mock
	private static ApplicationContext context;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {

		MockEnvironment properties = new MockEnvironment();
		// Where our test rules sit
		properties.setProperty(OrchestrationConfig.PROPERTIES_PACKAGES+"[0]", "au.com.kahaara.wf.orchestration.rules.testrules");

		ruleList = new RuleList(properties, context);


		// Force the creation of the rules since we are not running with springboottest
		WorkflowStart start = new WorkflowStart();
		RuleType rt = new RuleType(OrchestrationConfig.WORKFLOW_START, start, null);
		ruleList.getRuleList().put(OrchestrationConfig.WORKFLOW_START, rt);
		WorkflowEnd end = new WorkflowEnd();
		rt = new RuleType(OrchestrationConfig.WORKFLOW_END, end);
		ruleList.getRuleList().put(OrchestrationConfig.WORKFLOW_END, rt);
		
		TestRule1 rule1 = new TestRule1();
		rt = new RuleType("ruleReturnTRUE", rule1);
		ruleList.getRuleList().put("ruleReturnTRUE".toUpperCase(), rt);

		TestRule2 rule2 = new TestRule2();
		rt = new RuleType("ruleReturnFALSE", rule1);
		ruleList.getRuleList().put("ruleReturnFALSE".toUpperCase(), rt);

		TestRule3 rule3 = new TestRule3();
		rt = new RuleType("ruleReturnTrueAgain", rule1);
		ruleList.getRuleList().put("ruleReturnTrueAgain".toUpperCase(), rt);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link RuleSet#RuleSet()}.
	 * @throws Exception 
	 */
	@Test
	void testRuleSet() throws Exception {
		RuleSet rs = new RuleSet("", ruleList);
	}

	/**
	 * Test method.
	 */
	@Test
	void testRuleSetString() {

		try {
			RuleSet rs = new RuleSet(rulesNumbers, ruleList);
		} catch (RulesException e) {
			fail("Unable to create rule set from string");
		}
	}

	/**
	 * Test method
	 *
	 */
	@Test
	void testRuleSetMissing() {

		try {
			RuleSet rs = new RuleSet(rulesMissing, ruleList);
			fail("Rule creation should have failed");
		} catch (RulesException e) {
			//("Unable to create rule set from string");
		}
	}

	/**
	 * Test method for
	 */
	@Test
	void testRuleSetRuleSetType() {
		try {
			RuleSet rs = new RuleSet(DefaultRuleSetType.CUSTOM, ruleList);
		} catch (RulesException e) {
			fail("Unable to create rule set from rule set type");
		}
	}

	/**
	 * Test method
	 */
	@Test
	void testSetRules() {
		try {
			RuleSet rs = new RuleSet();
			rs.setRules(rulesNoNumbers, ruleList);
		} catch (RulesException e) {
			fail("Unable to create rule set from string");
		}
	}

	/**
	 * Test method for {@link RuleSet#getRules()}.
	 */
	@Test
	void testGetMainRules() {
		try {
			RuleSet rs = new RuleSet(rulesNumbers, ruleList);
			List<Rule> ruleList = rs.getRules();
		} catch (RulesException e) {
			fail("Unable to read main ruleSet");
		}
	}

	/**
	 * Test method for {@link RuleSet#setRules(java.util.List)}.
	 */
	@Test
	void testSetMainRules() {
		try {
			RuleSet rs = new RuleSet(rulesNoNumbers, ruleList);
			List<Rule> ruleList = rs.getRules();
			rs.setRules(ruleList);
		} catch (RulesException e) {
			fail("Unable to write main ruleSet");
		}
	}

	/**
	 * Test method for {@link RuleSet#setRules(java.util.List)}.
	 */
	@Test
	void testSetMainRulesDups() {
		try {
			RuleSet rs = new RuleSet(rulesDuplicate, ruleList);
			List<Rule> ruleList = rs.getRules();
			rs.setRules(ruleList);
			fail("Should not have been able to create rules");
		} catch (RulesException e) {
			// expected
		}
	}
}
