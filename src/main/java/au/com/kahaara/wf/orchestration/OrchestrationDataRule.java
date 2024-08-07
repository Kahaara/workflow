package au.com.kahaara.wf.orchestration;

import au.com.kahaara.wf.OrchestrationConfig;
import au.com.kahaara.wf.orchestration.event.EventType;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.rules.RuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A bunch of static methods to enable enforcement of the orcesutration rules as defined in each
 * workflow rule using the {@link RuleInfo} provided in each workflow rule class. Each test is accessed
 * simply by calling the public method
 * {@link #testInfoRequirements(OrchestrationConfig, OrchestrationData, String) testInfoRequirements}
 * at the start of each rule.
 * <P>During the rule the following methods are used depending on the data access
 * <ul>
 *     <li>{@link #testDataReadRequirements(OrchestrationConfig, OrchestrationData, String) testDataReadRequirements}</li>
 *     <li>{@link #testDataWriteRequirements(OrchestrationConfig, OrchestrationData, String) testDataWriteRequirements}</li>
 *     <li>{@link #testDataOptionalReadRequirements(OrchestrationConfig, OrchestrationData, String) testDataOptionalReadRequirements}</li>
 * </ul>
 * </P>
 */
public class OrchestrationDataRule {

	public static final Logger log = LoggerFactory.getLogger(OrchestrationDataRule.class);

	private OrchestrationDataRule() {
		// Nothing to see here
	}

	/**
	 * Test that the data marked as required in the rule getInfo is present as expected.
	 *
	 * @param orchestrationConfig  The configuration parameters for how to process the rules
	 * @param orchestrationData The {@link OrchestrationData} previously populated
	 * @param name The name of the rule that is being run
	 * @return a boolean to indicate test success or failure
	 * @throws RulesException If the rule processing is enforced and finds something misgiving
	 */
	public static boolean testInfoRequirements(OrchestrationConfig orchestrationConfig, OrchestrationData orchestrationData, String name) throws RulesException {

		RuleInfo info = orchestrationData.getCurrentRuleInfo();
		boolean ok = false;
		log.debug("Testing rule data requirements for rule {}",name);
		if (info != null) {
			ok = testIfMappedDataPresent(orchestrationConfig,  info, orchestrationData);
			if (ok) {
				ok = testIfResponseDataPresent(orchestrationConfig,  info, orchestrationData);
			}
			if (ok) {
				ok = testIfResponseDataPresentForUpdate(orchestrationConfig,  info, orchestrationData);
			}
			if (ok) {
				ok = testIfResponseDataPresentBeforeCreate(orchestrationConfig,  info, orchestrationData);
			}
		}
		return ok;
	}

	/**
	 * Tests the info about the rule to see if the data is available for retrieval
	 * depending on how the rule info was configured for that workflow rule
	 * <p>If something unexpected is found it will throw an exception</p>
	 * <p>Note: for loops not streams as small amount of data</p>
	 * <p>Only tests if ruleInfo checking enforcement is on</p>
	 *
	 * @param orchestrationConfig  The configuration parameters for how to process the rules
	 * @param orchestrationData The orchestration data to be tested
	 * @param name The class name of the object to examine
	 * @return true if the test criteria are met.
	 * @throws RulesException If the rule processing is enforced and finds something misgiving
	 */
	static boolean testDataReadRequirements(OrchestrationConfig orchestrationConfig, OrchestrationData orchestrationData, String name) throws RulesException {

		// This flag is to indicate if the name is found in the rule info and thus we
		// know it's intent.
		boolean found = false;
		RuleInfo info = orchestrationData.getCurrentRuleInfo();

		if (orchestrationConfig.isRuleInfoEnforce() || orchestrationConfig.isRuleInfoReported()) {
			// See if the class exists as the type to create AND that it doesn't exist as data
			found = testDataRequirementsForCreate(orchestrationConfig, orchestrationData, name, true) ;

			if (!found ) {
				found = testDataRequirementsForRead(orchestrationConfig, orchestrationData, name, true);
			}

			// Check that the intent was recorded in the ruleInfo
			if (!found) {
				// Why is the data not defined in the rule info
				if (orchestrationConfig.isRuleInfoEnforce()) {
					throw new RulesException("The defined rule information (RuleInfo) has not recorded the intent of this data for "+name);
				} else if (orchestrationConfig.isRuleInfoReported()) {
					log.error("The defined rule information (RuleInfo) has not recorded the intent of this data for {}",name);
				}

			}
		} else {
			// Checks are turned off. Why oh why would you
			found = true;
		}
		return found;
	}

	/**
	 * Tests the info about the rule to see if the data is available for insert or update
	 * depending on how the rule info was configured for that workflow rule
	 * <p>If something unexpected is found it will throw an exception</p>
	 * <p>Note: for loops not streams as small amount of data</p>
	 * <p>Only tests if ruleInfo checking enforcement is on</p>
	 *
	 * @param orchestrationConfig  The configuration parameters for how to process the rules
	 * @param orchestrationData The orchestration data to be tested
	 * @param name The class name of the object to examine
	 * @return True if found or if optional and marked as optional in the rule info.
	 * @throws RulesException If the rule processing is enforced and finds something misgiving
	 */
	static boolean testDataWriteRequirements(OrchestrationConfig orchestrationConfig, OrchestrationData orchestrationData, String name) throws RulesException {

		// This flag is to indicate if the name is found in the rule info and thus we
		// know it's intent.
		boolean found = false;
		RuleInfo info = orchestrationData.getCurrentRuleInfo();

		if (orchestrationConfig.isRuleInfoEnforce() || orchestrationConfig.isRuleInfoReported()) {
			// See if the class exists as the type to create AND that it doesn't
			// exist as data
			found = testDataRequirementsForCreate(orchestrationConfig, orchestrationData, name, false) ;

			if (!found ) {
				found = testDataRequirementsForUpdate(orchestrationConfig, orchestrationData, name, false);
			}

			// Check that the intent was recorded in the ruleInfo
			if (!found) {
				// Why is the data not defined in the rule info
				if (orchestrationConfig.isRuleInfoEnforce()) {
					throw new RulesException("Defined rule information (RuleInfo) has not recorded the intent of this data for "+name);
				} else if (orchestrationConfig.isRuleInfoReported()) {
					log.error("Defined rule information (RuleInfo) has not recorded the intent of this data for {}",name);
				}

			}
		} else {
			// Checks are turned off. Why oh why would you
			found = true;
		}
		return found;
	}

	/**
	 * Test if the data is optional
	 *
	 * @param orchestrationConfig  The configuration parameters for how to process the rules
	 * @param orchestrationData The orchestration data to be tested
	 * @param name The class name of the object to examine
	 * @return true if the optional flag is set or false otherwise
	 */
	static boolean testDataOptionalReadRequirements(OrchestrationConfig orchestrationConfig, OrchestrationData orchestrationData, String name) throws RulesException {
		boolean found = false;
		RuleInfo info = orchestrationData.getCurrentRuleInfo();

		if (orchestrationConfig.isRuleInfoEnforce() || orchestrationConfig.isRuleInfoReported()) {
			// See if it is data to be read as optional data
			if (info.getOptionalResponseClasses().stream().anyMatch(s -> s.equalsIgnoreCase(name))) {
				found = true;
			}
		} else {
			// Checks are turned off. Why oh why would you
			found = true;
		}
		return found;
	}

	/**
	 * Test for any data that is present when it shouldn't be
	 *
	 * @param orchestrationConfig  The configuration parameters for how to process the rules
	 * @param orchestrationData The orchestration data to be tested
	 * @param name The class name of the object to examine
	 * @return true if the data requirements are met for create
	 * @throws RulesException If the rule processing is enforced and finds something misgiving
	 */
	private static boolean testDataRequirementsForCreate(OrchestrationConfig orchestrationConfig, OrchestrationData orchestrationData, String name, boolean readingData) throws RulesException {
		boolean found = false;
		RuleInfo info = orchestrationData.getCurrentRuleInfo();

		for (String s : info.getCreatedResponseClasses()) {
			if (s.equalsIgnoreCase(name)) {
				testDataExistsForCreate(orchestrationConfig, orchestrationData, name, readingData);
				// Found an entry
				found = true;
				// SInce it'a about to be inserted we better move the insert to an update
				info.getModifiedResponseClasses().add(name);
				info.getCreatedResponseClasses().remove(name);
				break;
			}
		}

		return found;
	}

	/**
	 * See how to report on the data find for objects which are marked as to be created
	 *
	 * @param orchestrationConfig  The configuration parameters for how to process the rules
	 * @param orchestrationData The orchestration data to be tested
	 * @param name The class name of the object to examine
	 * @throws RulesException If the rule processing is enforced and finds something misgiving
	 */
	private static void testDataExistsForCreate(OrchestrationConfig orchestrationConfig, OrchestrationData orchestrationData, String name, boolean readingData) throws RulesException {
		if (readingData) {
			// On getData so it's a blanket NO
			if (orchestrationConfig.isRuleInfoEnforce()) {
				throw new RulesException("Attempting to read data for " + name + " when marked for creation only");
			} else if (orchestrationConfig.isRuleInfoReported()) {
				log.error("Attempting to read data for {} when marked for creation only", name);
			}
		} else {
			// On putData so the data should not already exist
			if (orchestrationData.resultData.get(name) != null) {
				//Data present when it shouldn't be
				if (orchestrationConfig.isRuleInfoEnforce()) {
					throw new RulesException("Create data for " + name + " already present in response data when marked for creation only");
				} else if (orchestrationConfig.isRuleInfoReported()) {
					log.error("Data for {} present in response data when it is marked for creation", name);
				}
			}
		}
	}

	/**
	 * This test covers any reads where the data should exist for get or modify. This test
	 * also covers data that is marked as optional that might exist.
	 *
	 * @param orchestrationConfig  The configuration parameters for how to process the rules
	 * @param orchestrationData The orchestration data to be tested
	 * @param name The class name of the object to examine
	 * @return returns true if the read requirements are met
	 * @throws RulesException If the rule processing is enforced and finds something misgiving
	 */
	private static boolean testDataRequirementsForRead(OrchestrationConfig orchestrationConfig, OrchestrationData orchestrationData, String name, boolean readingData) throws RulesException {

		RuleInfo info = orchestrationData.getCurrentRuleInfo();
		boolean found = info.getExpectedResponseClasses().stream().anyMatch(s -> s.equalsIgnoreCase(name));
		boolean optional = false;
		String type = "expected";

		// See if it is data to be read as optional data
		if (info.getOptionalResponseClasses().stream().anyMatch(s -> s.equalsIgnoreCase(name))) {
			type = "optional";
			optional = true;
			found = true;
		}

		if (found && !optional && orchestrationData.resultData.get(name) == null) {
			//Data not present when it should be unless optional
			if (orchestrationConfig.isRuleInfoEnforce()) {
				if (readingData) {
					throw new RulesException("Existing data for " + name + " unavailable for reading when marked as "+type);
				} else {
					throw new RulesException("Existing data for " + name + " unavailable in response data for updating when marked foasr "+type);
				}
			} else if (orchestrationConfig.isRuleInfoReported()) {
				if (readingData) {
					log.error("Existing data for {} unavailable for reading when marked as {}", name, type);
				} else {
					log.error("Existing data for {} unavailable in response data for updating when marked as {}", name, type);
				}
			}

		}

		return found;
	}

	/**
	 * This test covers any reads where the data should exist for get or modify. This test
	 * also covers data that is marked as optional that might exist.
	 *
	 * @param orchestrationConfig  The configuration parameters for how to process the rules
	 * @param orchestrationData The orchestration data to be tested
	 * @param name The class name of the object to examine
	 * @throws RulesException If the rule processing is enforced and finds something misgiving
	 */
	private static boolean testDataRequirementsForUpdate(OrchestrationConfig orchestrationConfig, OrchestrationData orchestrationData, String name, boolean readingData) throws RulesException {

		RuleInfo info = orchestrationData.getCurrentRuleInfo();
		// See if it is data to be modified
		boolean found = info.getModifiedResponseClasses().stream().anyMatch(s -> s.equalsIgnoreCase(name));
		boolean optional = false;
		String type = "modify";

		// See if it is data to be read as optional data
		if (info.getOptionalResponseClasses().stream().anyMatch(s -> s.equalsIgnoreCase(name))) {
			type = "optional";
			optional = true;
		}

		if (found && !optional && orchestrationData.resultData.get(name) == null) {
			//Data not present when it should be unless optional
			if (orchestrationConfig.isRuleInfoEnforce()) {
				if (readingData) {
					throw new RulesException("Existing data for " + name + " unavailable for reading when marked as "+type);
				} else {
					throw new RulesException("Existing data for " + name + " unavailable in response data for updating when marked foasr "+type);
				}
			} else if (orchestrationConfig.isRuleInfoReported()) {
				if (readingData) {
					log.error("Existing data for {} unavailable for reading when marked as {}", name, type);
				} else {
					log.error("Existing data for {} unavailable in response data for updating when marked as {}", name, type);
				}
			}

		}

		return found;
	}

	/**
	 * Test to see if we have the correct mapped data from the original request
	 *
	 * @param info The {@link RuleInfo} object
	 * @param request The {@link OrchestrationData} object
	 * @return A true if the test passed
	 */
	private static boolean testIfMappedDataPresent(OrchestrationConfig orchestrationConfig, RuleInfo info, OrchestrationData request) throws RulesException {

		boolean ok = true; // default to true if empty list
		log.trace("Testing if mapped data present");
		for (String i : info.getExpectedRequestClasses()) {
			if (request.getRequestData().containsKey(i)) {
				log.trace("Found mapped request data for {}",i);
				ok = true;
			} else {
				request.getStatus().addEvent(InfoType.ERROR, EventType.EXCEPTION, OrchestrationErrorCode.ORCH5003, "Missing mapped request data for "+i);
				if (orchestrationConfig.isRuleInfoEnforce()) {
					throw new RulesException("Missing mapped request data for "+i);
				} else if (orchestrationConfig.isRuleInfoReported()) {
					log.error("Missing mapped request data for {}",i);
				}
				ok = false;
				break;
			}
		}
		return ok;
	}

	/**
	 * Is the rule response data present that you want to read
	 *
	 * @param info The {@link RuleInfo} object
	 * @param request The {@link OrchestrationData} object
	 * @return A true if the test passed
	 */
	private static boolean testIfResponseDataPresent(OrchestrationConfig orchestrationConfig, RuleInfo info, OrchestrationData request) throws RulesException {

		boolean ok = true; // default to true if empty list
		log.trace("Testing if response data present");
		for (String i : info.getExpectedResponseClasses()) {
			if (request.getResultData().containsKey(i)) {
				log.trace("Found response data for {}",i);
				ok = true;
			} else {
				request.getStatus().addEvent(InfoType.ERROR, EventType.EXCEPTION, OrchestrationErrorCode.ORCH5003, "Missing response data for reading "+i);
				if (orchestrationConfig.isRuleInfoEnforce()) {
					throw new RulesException("Missing response data for reading "+i);
				} else if (orchestrationConfig.isRuleInfoReported()) {
					log.error("Missing response data for reading {}",i);
				}
				ok = false;
				break;
			}
		}
		return ok;
	}

	/**
	 * Test that the response data has been created by a previous rule prior to calling this rule
	 *
	 * @param info The {@link RuleInfo} object
	 * @param request The {@link OrchestrationData} object
	 * @return A true if the test passed
	 */
	private static boolean testIfResponseDataPresentForUpdate(OrchestrationConfig orchestrationConfig, RuleInfo info, OrchestrationData request) throws RulesException {

		boolean ok = true; // default to true if empty list
		log.trace("Testing if response data present for update");
		for (String i : info.getModifiedResponseClasses()) {
			if (request.getResultData().containsKey(i)) {
				log.trace("Found response data for {}",i);
				ok = true;
			} else {
				request.getStatus().addEvent(InfoType.ERROR, EventType.EXCEPTION, OrchestrationErrorCode.ORCH5003, "Missing response data for update "+i);
				if (orchestrationConfig.isRuleInfoEnforce()) {
					throw new RulesException("Missing response data for update "+i);
				} else if (orchestrationConfig.isRuleInfoReported()) {
					log.error("Missing response data for update {}",i);
				}
				ok = false;
				break;
			}
		}
		return ok;
	}

	/**
	 * Test to ensure that no data exists for the listed object classes prior to running the rule
	 *
	 * @param info The {@link RuleInfo} object
	 * @param request The {@link OrchestrationData} object
	 * @return A true if the test passed
	 */
	private static boolean testIfResponseDataPresentBeforeCreate(OrchestrationConfig orchestrationConfig, RuleInfo info, OrchestrationData request) throws RulesException {

		boolean ok = true; // default to true if empty list
		log.trace("Testing if response data present before create");
		for (String i : info.getCreatedResponseClasses()) {
			if (request.getResultData().containsKey(i)) {
				request.getStatus().addEvent(InfoType.ERROR, EventType.EXCEPTION, OrchestrationErrorCode.ORCH5003, "Data present before create "+i);
				if (orchestrationConfig.isRuleInfoEnforce()) {
					throw new RulesException("Data present before create "+i);
				} else if (orchestrationConfig.isRuleInfoReported()) {
					log.error("Data present before create {}",i);
				}
				ok = false;
				break;
			} else {
				log.trace("Data not present before create {}",i);
				ok = true;
			}
		}
		return ok;
	}

}
