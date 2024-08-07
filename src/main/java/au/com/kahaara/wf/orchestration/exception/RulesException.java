package au.com.kahaara.wf.orchestration.exception;

/**
 * If any exception is thrown in the processing of rules or rule sets then 
 * they are converted to a RuleException see {@link Exception} for more info
 * 
 * @author Simon Haddon
 *
 */
public class RulesException extends Exception {

	private static final long serialVersionUID = 4692047628588909380L;

	/**
	 * Creates a new exception
	 *
	 * @param errorMessage The associated error message
	 */
	public RulesException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Create a new exception with the previous exception details. Would be used when rethrowing an
	 * existing exception
	 *
	 * @param errorMessage The associated error message
	 * @param exception The previously thrown exception
	 */
	public RulesException(String errorMessage, Throwable exception) {
		super(errorMessage);
		this.addSuppressed(exception);
	}

}
