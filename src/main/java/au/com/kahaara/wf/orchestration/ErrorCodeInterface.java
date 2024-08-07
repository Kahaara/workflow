package au.com.kahaara.wf.orchestration;

/**
 * This interface must be extended by any enum that is to be used for error codes within the orchestration
 * which has event recording as part of it.
 * <P>The getDescription must return the description of the Enum where each enum element is formatted
 * with the single parameter which gives a human readable meaning behind the error code i.e:
 * <pre>
 * public enum OrchestrationErrorCode implements ErrorCodeInterface {
 *   / ** System error codes * /
 *   ORCH1001("Unknown exception caught.");
 * }
 *  * </pre>
 * @author excdsn
 *
 */
public interface ErrorCodeInterface {

	/**
	 * Get the description attached to the enum type
	 * 
	 * @return The description
	 */
	String getDescription();
	
}
