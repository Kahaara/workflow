package au.com.kahaara.wf.orchestration.rules;

import au.com.kahaara.wf.orchestration.exception.RulesException;

import java.util.ArrayList;
import java.util.List;

/**
 * <P>This contains information about the orchestration rule that is being implemented. It is created by a workflow
 * rule from the getInfo() method. If not used then the method needs to examine each expected data object to ensure
 * it exists and also create an event if not.</P>
 * <P>To define the intent of any read/write access on the data then this must be defined using the following
 * methods
 *   <ol>
 *     <li>addExpectedRequestData - Data is expected in the request object as a result of the mapping process</li>
 *     <li>addCreatedResponseData - Data should not exist and will be added by the current rule. If data is updated
 *     after being created in the same rule then this is allowed</li>
 *     <li>addModifiedResponseData - Data must exist and is expected to be modified</li>
 *     <li>addExpectedResponseData - Data is expected to exist but is read only</li>
 *     <li>addOptionalResponseData - Data may exist and a null is returned if not available</li>
 *   </ol>
 * </P>
 * <P>If data is created, read or modified and the intent is not defined correctly then an
 * {@link RulesException} is thrown. This behaviour is modifiable using properties </P>
 * 
 * @author excdsn
 *
 */
public class RuleInfo {

	/**
	 * Data that must be present in the initial orchestration data
	 */
	List<String> mappingData = new ArrayList<>();

	/**
	 * Data that must exist in the response data prior to calling this rule
	 */
	List<String> responseData = new ArrayList<>();

	/**
	 * Data that might exist in the response data prior to calling this rule
	 */
	List<String> optionalResponseData = new ArrayList<>();

	/**
	 * Data that must exist and is also going to be modified
	 */
	List<String> modifiedDataClasses = new ArrayList<>();

	/**
	 * Data that should not exist prior to this class being called
	 */
	List<String> createdDataClasses = new ArrayList<>();

	/**
	 * The object class here is expected to exist in the orchestration data prior to calling this rule
	 *
	 * @param objectClass The {@link Class} object
	 * @param <T> The specific class abstraction
	 */
	public  <T> void addExpectedRequestData(Class<T> objectClass) {
		mappingData.add(objectClass.getName());
	}

	/**
	 * The object class here is expected to exist in the response prior to calling this rule. This
	 * data would have been created by a previous rule
	 *
	 * @param objectClass The {@link Class} object
	 * @param <T> The specific class abstraction
	 */
	public  <T> void addExpectedResponseData(Class<T> objectClass) {
		responseData.add(objectClass.getName());
		if (this.getOptionalResponseClasses().stream().anyMatch(s -> s.equalsIgnoreCase(objectClass.getName()))) {
			this.optionalResponseData.remove(objectClass.getName());
		}
	}

	/**
	 * The object class here is optional in the response prior to calling this rule. This
	 * data might have been created by a previous rule. If added here then the data can also be
	 * modified or created according to if it exists or not
	 *
	 * @param objectClass The {@link Class} object
	 * @param <T> The specific class abstraction
	 */
	public  <T> void addOptionalResponseData(Class<T> objectClass) {
		optionalResponseData.add(objectClass.getName());
		if (this.getExpectedResponseClasses().stream().anyMatch(s -> s.equalsIgnoreCase(objectClass.getName()))) {
			this.responseData.remove(objectClass.getName());
		}
	}

	/**
	 * The object class here is expected to exist in the response prior to calling this rule. This
	 * data would have been created by a previous rule and will be modified by this rule.
	 * <P>The behavior can be overridden by placing the object class name into the optional data
	 * using {@link #addOptionalResponseData(Class)}</P>
	 *
	 * @param objectClass The {@link Class} object
	 * @param <T> The specific class abstraction
	 */
	public  <T> void addModifiedResponseData(Class<T> objectClass) {
		modifiedDataClasses.add(objectClass.getName());
		if (!this.getExpectedResponseClasses().stream().anyMatch(s -> s.equalsIgnoreCase(objectClass.getName()))
		   && !this.getOptionalResponseClasses().stream().anyMatch(s -> s.equalsIgnoreCase(objectClass.getName()))) {
			// Also add it to the required response data
			responseData.add(objectClass.getName());
		}
	}

	/**
	 * The object class here is not expected to exist in the response prior to calling this rule.
	 *
	 * @param objectClass The {@link Class} object
	 * @param <T> The specific class abstraction
	 */
	public  <T> void addCreatedResponseData(Class<T> objectClass) {
		createdDataClasses.add(objectClass.getName());
	}

	/**
	 * Get the expected request data
	 * @return A list of class names expected as data
	 */
	public List<String> getExpectedRequestClasses() {
		return mappingData;
	}

	/**
	 * Get the expected response data
	 * @return A list of class names expected as data
	 */
	public List<String> getExpectedResponseClasses() {
		return responseData;
	}

	/**
	 * Get the optional response data
	 * @return A list of class names expected as data
	 */
	public List<String> getOptionalResponseClasses() {
		return optionalResponseData;
	}

	/**
	 * Get the expected response data that can be modified
	 * @return A list of class names expected as data
	 */
	public List<String> getModifiedResponseClasses() {
		return modifiedDataClasses;
	}

	/**
	 * Get the classes that should not be present as data as they are to be created
	 * @return A list of class names expected as data
	 */
	public List<String> getCreatedResponseClasses() {
		return createdDataClasses;
	}


}
