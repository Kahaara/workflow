/**
 * 
 */
package au.com.kahaara.wf.orchestration.rules.testrules;

import au.com.kahaara.wf.orchestration.OrchestrationData;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.rules.RuleInfo;
import au.com.kahaara.wf.orchestration.rules.RuleResult;
import au.com.kahaara.wf.orchestration.workflow.Workflow;
import au.com.kahaara.wf.orchestration.workflow.WorkflowRuleInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * @author excdsn
 *
 */
@Workflow (rulename="RuleReturnTrue")
public class TestRule1 implements WorkflowRuleInterface {

	public static final Logger log = LoggerFactory.getLogger(TestRule1.class);
	
	@Override
	public RuleInfo getInfo() {
		RuleInfo r =  new RuleInfo();
		
		r.addCreatedResponseData(String.class);
		r.addExpectedResponseData(String.class);
		
		return r;
	}

	@Override
	public RuleResult runRule(OrchestrationData request) throws RulesException {
		log.info("Starting orchestration for "+request.getRuleSetType());
		RuleResult retVal = new RuleResult(true);
		// we used this to test the "getting nothing back" scenario between .getData & .getOptionalData
		String sytr = "This is a string";
		request.putData(sytr);
		sytr = (String) request.getData(String.class);
		sytr = sytr.toLowerCase(Locale.ROOT);

		return retVal ;
	}

}
