package au.com.kahaara.wf.orchestration;

import au.com.kahaara.wf.OrchestrationConfig;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.rules.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.Assert;

/**
 * When a service request mapping is performed then the results of the mapping are
 * placed into this mapped request data object. The object is then the gateway to the
 * orchestration workflow.
 * <P>It is expected that the mapping also sets the request type {@link RuleSetTypeInterface} so that the
 * orchestration layer can then process the request correctly.
 * <P>The main data for assembling are available on the {@link OrchestrationData#resultData} map that can be
 * retrieved or put using the following example
 * <P><pre>
 *  TravelDocument td = (TravelDocument) data.getRequestData(TravelDocument.class);
 *  data.putData(td);
 * </pre>
 * 
 * @author excdsn
 *
 */

@Configurable(preConstruction = true)
public class OrchestrationData {
	
	public static final Logger log = LoggerFactory.getLogger(OrchestrationData.class);

	/**
	 * Config bean . It is used to tell if deep copy is allowed.
	 */
	private OrchestrationConfig orchestrationConfig;

	/**
	 * The request type according the available {@link RuleSetTypeInterface}
	 */
	private RuleSetTypeInterface type = DefaultRuleSetType.UNKNOWN;
	
	/**
	 * If set then these rules are use in place of any others
	 */
	private String customRules;

	/**
	 * What is the current rule about to be executed
	 */
	private Rule currentRule;
	/**
	 * And what is the current rule info
	 */
	private RuleInfo currentRuleInfo;

	/**
	 * Used for deep copying objects from the request map.
	 */
	ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * See {@link Status}
	 */
	private Status status = new Status();
	
	/**
	 * The rule set specific to this request
	 * See {@link RuleSet}
	 */
	private RuleSet ruleSet;

	/**
	 * String containing the original request serialised.
	 */
	private String requestMessage;

	/**
	 * A map of any request data that has been mapped by the mapper classes.
	 * Each entry must be the class and the data for retrieval and manipulation in 
	 * the orchestration.
	 */
	private Map<String, Object> requestData = new HashMap<>();

	/**
	 * A map of any data created as part of orchestration.
	 * Each entry must be the class and the data for retrieval and manipulation in
	 * the orchestration.
	 */
	Map<String, Object> resultData = new HashMap<>();

	/**
	 * The test results when running orchestration in test mode.
	 */
	private List<String> testResults = new ArrayList<>();

	/**
	 * This flag indicated if we are processing within the orchestration or not.
	 * Once in orchestration then this flag is set to true indicating that nothing
	 * should be written to the mapped data.  At this stage it doesn't prevent data
	 * that is referenced being modified but it is expected that will change so that
	 * an object returned is a deep copy and no updates are allowed
	 */
	private boolean processingRules = false;

	/**
	 * Purely for sub rules. i.e: When you want to make a call to a rule that then
	 * creates a new set of rules.
	 */
	private Orchestration orchestration;

	/**
	 * Instantiate a new instance of a RuleRequestWrapper object.
	 *
	 * @param orchestrationConfig  The orchestration configuration data
	 * @param type The {@link RuleSetTypeInterface}
	 */
	public OrchestrationData(OrchestrationConfig orchestrationConfig, RuleSetTypeInterface type) {
		this.orchestrationConfig = orchestrationConfig;
		this.type = type;
	}

	/**
	 * Instantiate a new instance of a RuleRequestWrapper object.
	 * This defaults the rule {@link RuleSetTypeInterface} to UNKNOWN.
	 *
	 * @param orchestrationConfig  The orchestration configuration data
	 */
	public OrchestrationData(OrchestrationConfig orchestrationConfig) {
		this.orchestrationConfig = orchestrationConfig;
	}

	/**
	 * Instantiate a new instance of a RuleRequestWrapper object.
	 * This defaults the rule {@link RuleSetTypeInterface} to UNKNOWN.
	 */
	public OrchestrationData() {

	}

	/**
	 * Get the orchestration object. This should be used for sub rule processing
	 *
	 * @return The Orchestration engine instance.
	 */
	public Orchestration getOrchestration() {
		return orchestration;
	}

	/**
	 * Just a package call. Set the orcestration engine for use in rules
	 *
	 * @param orchestration The orchestration engine
	 */
	void setOrchestration(Orchestration orchestration) {
		this.orchestration = orchestration;
	}

	/**
	 * @return the request type
	 */
	public RuleSetTypeInterface getRuleSetType() {
		return this.type;
	}

	/**
	 * @param type the type to set
	 */
	public void setRuleSetType(RuleSetTypeInterface type) {
		this.type = type;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * @return the status InfoType
	 */
	public InfoType getStatusInfoType() {
		return status.getStatus();
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return the ruleSet
	 */
	public RuleSet getRuleSet() {
		return ruleSet;
	}

	/**
	 * @param ruleSet the ruleSet to set
	 */
	public void setRuleSet(RuleSet ruleSet) {
		this.ruleSet = ruleSet;
	}

	/**
	 * Get data from the {link {@link #requestData} map. If the
	 * data is not found then null is returned. If the data is not present then an
	 * exception will be thrown
	 * <p><b>NB:</b> This performs a deep copy by default as once data is placed into this
	 * as a result of mapping then it should not be modified. </p>
	 * @see java.util.Map
	 * 
	 * @param objectClass The object class name
	 * @param <T> The object type
	 * @return the object if found and is deep copied successfully.
	 */
	public  <T> Object getRequestData(Class<T> objectClass) {
		
		// Requires a deep copy if processingRules is true
		return getRequestData(objectClass, processingRules, true);
		
	}

	/**
	 * Get data from the {link {@link #requestData} map. If the
	 * data is not found then null is returned.
	 * <p><b>NB:</b> This performs a deep copy by default as once data is placed into this
	 * as a result of mapping then it should not be modified. </p>
	 * @see java.util.Map
	 *
	 * @param objectClass The object class name
	 * @param <T> The object type
	 * @return the object if found and is deep copied successfully.
	 */
	public  <T> Object getOptionalRequestData(Class<T> objectClass) {
		
		// Requires a deep copy if processingRules is true
		return getRequestData(objectClass, processingRules, false);
		
	}

	/**
	 * Get data from the {link {@link #requestData} map. If the
	 * data is not found then null is returned. If the deepcopy flag is true
	 * then attempt to make a deep copy using fasterxml. If that fails then return the
	 * data pointer to request object.
	 *
	 * @see java.util.Map
	 *
	 * @param objectClass The object class name
	 * @param deepcopy Flag to indicate if this is a deep copy or not
	 * @param required flag to indicate if the field is required and if so then throw an exception
	 * @param <T> The object type
	 * @return The object if found.
	 */
	public  <T> Object getRequestData(Class<T> objectClass, boolean deepcopy, boolean required) {

		if (!this.isDeepCopyAvailable()) {
			deepcopy = false;
		}
		log.trace("Get request for {}. deep copy={}",objectClass.getName(),deepcopy);
		Object data = requestData.get(objectClass.getName());
		if (required) {
			Assert.notNull(data, objectClass.getSimpleName() + " must not be null!");
		}
		if (!deepcopy) {
			return data;
		}
		try {
			return objectMapper.readValue(objectMapper.writeValueAsString(data), objectClass);
		} catch (JsonProcessingException e) {
			log.error("Unable to deep copy object {} {}",objectClass,e);
		}
		return data;
	}

	/**
	 * Set the {@link #requestData} for that object class type &lt;T&gt;. 
	 * If the map previously 
	 * contained a mapping for the key, the old value is replaced by the new value.
	 * <P>NB: I have used "put" as this is relating to multiple possible entries allowed
	 * in an Map instance rather that "get" which implies just 1 value.
	 * @see java.util.Map
	 * 
	 * @param <T> The object type
	 * @param data The data for the object class
	 * @throws RulesException If putting data in when the rules are running
	 */
	public <T> void putRequestData(T data) { // throws RulesException
		if (processingRules) {
			//TODO: Change to exception 
			log.error("Unable to put data into orchestration request data once processing rules");
			//throw new RulesException("Unable to put data into orchestration request data once processing rules")
			return;
		}
		log.trace("Set request for {}",data.getClass().getName());
		requestData.put(data.getClass().getName(), data);
	}

	/**
	 * @return the requestData
	 */
	public Map<String, Object> getRequestData() {
		return requestData;
	}

	/**
	 * @param requestData the requestData to set
	 */
	public void setRequestData(Map<String, Object> requestData) {
		if (processingRules) {
			log.error("Unable to put data into orchestration request data once processing rules");
		}
		this.requestData = requestData;
	}

	/**
	 * @return the resultData
	 */
	public Map<String, Object> getResultData() {
		return resultData;
	}

	/**
	 * @param resultData the resultData to set
	 */
	public void setResultData(Map<String, Object> resultData) {
		this.resultData = resultData;
	}

	/**
	 * Get data from the {link {@link #resultData} map. If the
	 * data is not found then null is returned. Don't default to a deep copy
	 *
	 * @see java.util.Map
	 *
	 * @param <T> The object type
	 * @param objectClass The object class name
	 * @return the object that was stored previously or null otherwise
	 * @throws RulesException This can only be thrown while running rules
	 * in the orchestration and only then, but it has to be defined anyway
	 */
	public <T> Object getData(Class<T> objectClass) throws RulesException {

		boolean required = true;

		if (this.isProcessingRules() && this.getCurrentRule() != null) {

			// Test and throw an exception or report the problem depending on settings
			OrchestrationDataRule.testDataReadRequirements(orchestrationConfig, this, objectClass.getName());

			// Is it optional
			if (OrchestrationDataRule.testDataOptionalReadRequirements(orchestrationConfig, this, objectClass.getName())) {
				required = false;
			}

		} else {
			// Not required as not processing rules
			required = false;
		}

		return this.getData(objectClass, false, required);
	}

	/**
	 * Get data from the {link {@link #resultData} map. If the
	 * data is not found then return a null
	 *
	 * @see java.util.Map
	 *
	 * @param <T> The object type
	 * @param objectClass The object class name
	 *
	 * @return the object
	 */
	@Deprecated
	public <T> Object getOptionalData(Class<T> objectClass) {
		return this.getData(objectClass, false, false);
	}

	/**
	 * Get data from the {link {@link #resultData} map. If the
	 * data is not found then null is returned.
	 * @see java.util.Map
	 *
	 * @param objectClass The object class name
	 * @param deepcopy Flag to indicate if this is a deep copy or not
	 * @param required flag to indicate if the data is required and if not present then thrown an exception.
	 * @param <T> The object type
	 * @return The object if found.
	 */
	private  <T> Object getData(Class<T> objectClass, boolean deepcopy, boolean required) {

		log.trace("Get response for {}. deep copy={}",objectClass.getName(),deepcopy);

		if (!this.isDeepCopyAvailable()) {
			deepcopy = false;
		}
		Object data = resultData.get(objectClass.getName());
		if (required) {
			if (orchestrationConfig.isRuleInfoEnforce()) {
				Assert.notNull(data, objectClass.getSimpleName() + " must not be null!");
			}
		}
		if (!deepcopy) {
			return data;
		}
		try {
			return objectMapper.readValue(objectMapper.writeValueAsString(data), objectClass);
		} catch (JsonProcessingException e) {
			log.error("Unable to deep copy object {} from response {}", objectClass, e.getMessage());
			return null;
		}
	}

	/**
	 * Set the {@link #resultData} for that object class type &lt;T&gt;.
	 * If the map previously
	 * contained a mapping for the key, the old value is replaced by the new value.
	 * <P>NB: I have used "put" as this is relating to multiple possible entries allowed
	 * in an Map instance rather that "get" which implies just 1 value.
	 * @see java.util.Map
	 *
	 * @param <T> The object type
	 * @param data The data for the object class
	 * @throws RulesException If the data is present when it's marked for creation or if
	 * the data is present and not marked for modification then an exception is thrown
	 */
	public <T> void putData(T data) throws RulesException {
		putData(data, false);
	}

	/**
	 * <P>DO NOT USE UNLESS ABSOLUTELY REQUIRED AS IT AVOIDS THE PROPER CHECKS</P>
	 * This is an alternative to putData which will allow for data to be placed onto the
	 * data map with no consideration of what is on there. It is the same
	 * as {@link #putData(Object)}
	 *
	 * @param <T> The object type
	 * @param data The data for the object class
	 * @throws RulesException If the data is present when it's marked for creation or if
	 * the data is present and not marked for modification then an exception is thrown
	 */
	private <T> void putForceData(T data) throws RulesException {
		putData(data, true);
	}

	private <T> void putData(T data, boolean force) throws RulesException {

		if (!force && this.isProcessingRules() && this.getCurrentRule() != null) {
			OrchestrationDataRule.testDataWriteRequirements(orchestrationConfig, this, data.getClass().getName());
		}

		log.trace("Set response data for {}",data.getClass().getName());
		this.resultData.put(data.getClass().getName(), data);
	}

	/**
	 * Return the results generated from a test run {@link OrchestrationMode}
	 * @return An array of test results
	 */
	public List<String> getTestResults() {
		return testResults;
	}

	/**
	 * Set the test results
	 * @param testResults The array of test results
	 */
	public void setTestResults(List<String> testResults) {
		this.testResults = testResults;
	}

	/**
	 * @return the message
	 */
	String getRequestMessage() {
		return this.requestMessage;
	}

	/**
	 * @param msg the message to set
	 */
	public void setRequestMessage(String msg) {
		this.requestMessage = msg;
	}

	/**
	 * @return the customRules
	 */
	public String getCustomRules() {
		return customRules;
	}

	/**
	 * @param customRules the customRules to set
	 */
	public void setCustomRules(String customRules) {
		this.customRules = customRules;
	}

	/**
	 * @return the current Rule
	 */
	public Rule getCurrentRule() {
		return currentRule;
	}

	/**
	 * @param currentRule the current rule to set
	 */
	public void setCurrentRule(Rule currentRule) {
		this.currentRule = currentRule;
	}

	/**
	 * Are we processing the rules yet?
	 * @return boolean flag
	 */
	public boolean isProcessingRules() {
		return processingRules;
	}

	/**
	 * Set the processing state
	 * @param processingRules flag to indicate of processing the rules
	 */
	public void setProcessingRules(boolean processingRules) {
		this.processingRules = processingRules;
	}


	/**
	 * Answers if deep copying is allowed .
	 * The null check is put in place to support a large number of existing unit test.
	 * @return true , if deep copy is configured to be allowed.
	 */
	public boolean isDeepCopyAvailable() {
		return orchestrationConfig != null && orchestrationConfig.isDeepCopyAllowed();
	}

	/**
	 * THIS IS ONLY TEMPORARILY VISIBLE (Public)
	 * @param info takes the RuleInfo from the currently running rule
	 */
	public void setCurrentRuleInfo(RuleInfo info) {
		this.currentRuleInfo = info;
	}

	/**
	 * THIS IS ONLY TEMPORARILY VISIBLE (Public)
	 * @return the RuleInfo from the currently running rule
	 */
	public RuleInfo getCurrentRuleInfo() {
		return this.currentRuleInfo;
	}


	/**
	 * gets the OrchestrationConfig in the OrchestrationData
	 * @return configuration of the current orchestration
	 */
	OrchestrationConfig getOrchestrationConfig() {
		return orchestrationConfig;
	}

	/**
	 * sets the OrchestrationConfig in the OrchestrationData
	 * @param orchestrationConfig configuration of the current orchestration
	 */
	void setOrchestrationConfig(OrchestrationConfig orchestrationConfig) {
		this.orchestrationConfig = orchestrationConfig;
	}
}
