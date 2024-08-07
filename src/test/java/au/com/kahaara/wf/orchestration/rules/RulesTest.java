package au.com.kahaara.wf.orchestration.rules;

import au.com.kahaara.wf.OrchestrationConfig;
import au.com.kahaara.wf.orchestration.OrchestrationMode;
import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.rules.def.WorkflowEnd;
import au.com.kahaara.wf.orchestration.rules.def.WorkflowStart;
import au.com.kahaara.wf.orchestration.workflow.WorkflowRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author excdsn
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "app.scheduling.enable=false")
class RulesTest {

	private RuleSet ruleSet = new RuleSet();
	private String rulesStr = "START,END";

	WorkflowRunner rules;

	@Mock
	private ApplicationContext context;

	private OrchestrationData request;

	private RuleList ruleList;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	void setUpBeforeClass() throws Exception {

		MockitoAnnotations.openMocks(this);

		MockEnvironment properties = new MockEnvironment();
		// Where our test rules sit
		properties.setProperty(OrchestrationConfig.PROPERTIES_PACKAGES+"[0]", "au.com.kahaara.wf.orchestration.rules.testrules");

		ruleList = new RuleList(properties, context);

		OrchestrationConfig oc = new OrchestrationConfig();
		oc.setRuleInfoReported(true);
		oc.setRuleInfoEnforce(false);

		request = new OrchestrationData(oc);
		request.setRuleSetType(DefaultRuleSetType.UNKNOWN);

		// Force the creation of the rules since we are not running with springboottest
		WorkflowStart start = new WorkflowStart();
		RuleType rt = new RuleType(OrchestrationConfig.WORKFLOW_START, start);
		ruleList.getRuleList().put(OrchestrationConfig.WORKFLOW_START, rt);
		WorkflowEnd end = new WorkflowEnd();
		rt = new RuleType(OrchestrationConfig.WORKFLOW_END, end);
		ruleList.getRuleList().put(OrchestrationConfig.WORKFLOW_END, rt);
		
	}

	
	/**
	 * @throws java.lang.Exception
	 */
//	@BeforeAll
//	static void setUpBeforeClass() throws Exception {
//		ruleSet = new RuleSet(rulesStr);
//	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test method
	 * @throws RulesException 
	 */
	@Test
	final void testRules() throws RulesException {
		ruleSet = new RuleSet("", ruleList);
		List<Rule> rulesList = ruleSet.getRules();
		if (rulesList.isEmpty()) {
			fail("Rules set should not be empty"); 
		}
	}

	/**
	 * Test method for {@link WorkflowRunner#processRules(OrchestrationData, OrchestrationMode)}.
	 */
	@Test
	final void testProcessRules() throws RulesException {
		ruleSet = new RuleSet("", ruleList);
		request.setRuleSet(ruleSet);
		try {
			RuleRunner rr = new RuleRunner();
			rules = new WorkflowRunner(rr);
			OrchestrationMode mode = OrchestrationMode.TEST;
			RuleResult result = rules.processRules(request, mode);
			if (!result.isOk()) {
				fail("Expected result data from test orchestration run");
			}
		} catch (RulesException e) {
			fail("Failed to run simple start,end rules"); 
		}
	}

}
