package au.com.kahaara.wf.orchestration.rules;

import java.util.Date;
import java.time.OffsetDateTime;

/**
 * Each rule must return a RuleResult object. Currently, it contains a boolean true/false flag which is used
 * by the orchestration workflow control and a string that can return information about the processing of
 * the rule
 * 
 * @author excdsn
 *
 */
public class RuleResult {
	
	/**
	 * A flag to indicate if this rule worked or not
	 */
	private Boolean ok = false;

	/**
	 * Used for when testing the orchestration
	 */
	private String testInfo = "";

	/**
	 * Used for sub rules if required
	 */
	private String resultInfo = "";
	
	/**
	 * Used for the rule's start time in assessment if required;
	 */
	private Date startTime = null;
	
	public RuleResult() {
		this(false);
	}
	
	public RuleResult(boolean ok) {
		setStartTime(Date.from(OffsetDateTime.now().toInstant()));
		this.ok = ok;
	}

	/**
	 * @return the ok
	 */
	public boolean getOk() {
		return isOk();
	}

	/**
	 * @return the ok
	 */
	public boolean isOk() {
		return ok;
	}

	/**
	 * @param ok the ok to set
	 */
	public void setOk(boolean ok) {
		this.ok = ok;
	}

	/**
	 * Used internally by the orchestration when in test mode
	 *
	 * @return The string of test info
	 */
	public String getTestInfo() {
		return testInfo;
	}

	/**
	 *  Used internally by the orchestration when in test mode
	 *
	 * @param testInfo Any test mode info to add
	 */
	public void setTestInfo(String testInfo) {
		this.testInfo = testInfo;
	}

	/**
	 * Get the result info if set from a previous rule
	 *
	 * @return A string
	 */
	public String getResultInfo() {
		return resultInfo;
	}

	/**
	 * Set result info about the rule if required
	 *
	 * @param resultInfo A String
	 */
	public void setResultInfo(String resultInfo) {
		this.resultInfo = resultInfo;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

}
