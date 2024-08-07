package au.com.kahaara.wf.orchestration.event;

import java.util.Date;

import au.com.kahaara.wf.orchestration.InfoType;
import au.com.kahaara.wf.orchestration.OrchestrationErrorCode;
import au.com.kahaara.wf.orchestration.ErrorCodeInterface;

/**
 * This stores the individual events that can occur as part of processing a workflow
 *
 */
public class Event {
	
	/**
	 * What is the information status. This should be OK while things are progressing normally
	 *  but can also be WARNING and ERROR.
	 */
	private InfoType infoType = InfoType.OK;
	
	/**
	 * Records the type of event. i.e: rule or exception
	 */
	private EventType eventType = EventType.RULE;
	
	/**
	 * If there is an associated id
	 */
	private Long transactionId = null;
	
	/**
	 * The error code of the message if there was a problem
	 * @see OrchestrationErrorCode
	 */
	private ErrorCodeInterface code;
	
	/**
	 * The message associated with the code. It defaults to the message that is defined in
	 * {@link OrchestrationErrorCode} but may be overridden 
	 */
	private String message;
	
	/**
	 * Any details about what went wrong. 
	 */
	private String details;

	/**
	 * When this event object is created
	 */
	private Date timestamp = new Date(System.currentTimeMillis());

	/**
	 * 
	 */
	public Event() {
		
	}
	
	/**
	 * Create a new event object
	 *
	 * @param type What {@link InfoType} is this
	 * @param eventType What {@link EventType} is this relating to
	 * @param code What is the error code from an implementation of {@link ErrorCodeInterface}
	 * @param message The message
	 * @param details The details about the event
	 */
	public Event(InfoType type, EventType eventType, ErrorCodeInterface code, String message, String details) {
		this.infoType = type;
		this.eventType = eventType;
		this.code = code;
		this.message = message;
		this.details = details;
	}
	
	/**
	 * @return the status
	 */
	public InfoType getInfoType() {
		return infoType;
	}

	/**
	 * @param infoType the status to set
	 */
	public void setInfoType(InfoType infoType) {
		this.infoType = infoType;
	}

	/**
	 * @return the eventType
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the transactionId
	 */
	public Long getTransactionId() {
		return transactionId;
	}

	/**
	 * @param transactionId the transactionId to set
	 */
	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * @return the code
	 */
	public ErrorCodeInterface getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(ErrorCodeInterface code) {
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the details
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(String details) {
		this.details = details;
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
}

