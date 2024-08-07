package au.com.kahaara.wf.orchestration;

/**
 * These are error codes that are internal to the orchestration engine
 * 
 * @author excdsn
 *
 */
public enum OrchestrationErrorCode implements ErrorCodeInterface {

	/** System error codes */
	ORCH1001("Unknown exception caught."),
	/** Missing Attribute in Request */
	ORCH5003("Missing attribute in request"),
	UNKNOWN ("Unknown Error");

	private String description;
	
	OrchestrationErrorCode(String error) {
		setDescription(error);
	}

	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	private void setDescription(String description) {
		this.description = description;
	}
}