package au.com.kahaara.wf.orchestration;

import java.util.ArrayList;
import java.util.List;

import au.com.kahaara.wf.orchestration.event.Event;
import au.com.kahaara.wf.orchestration.event.EventType;

/**
 * Store any workflow and error status as a result of running any orchestration workflow or rule.
 * A status other than OK does not stop orchestration but records the event which can then be processed
 * by the next stage if required.
 * <P>The main entry to this can as<br/>
 * <code>
 *  Status status = new Status();
 *  status.addEvent(type, code, details);
 * </code> 
 * <P>or<br/>
 * <code>
 *  Status status = new Status(type, code, details);
 * </code>
 * <P>Once the object exists and is initialized then you can call<br/>
 * <code>
 *  status.addEvent(type, code, details);
 * </code> 
 * 
 * @author Simon Haddon
 *
 */
public class Status {
	
	/**
	 * What is the information status. This should be OK while things are progressing normally
	 *  but can also be WARNING and ERROR. This status is the overall status which will be the highest
	 *  level according the EventMessage information
	 */
	private InfoType infoStatus = InfoType.OK;
	
	/**
	 * Store an array of events that occurred during processing of the orchestration workflow rules
	 */
	private List<Event> events = new ArrayList<>();
	
	/**
	 * Instantiate a new Status object
	 */
	public Status() {
		
	}
	
	/**
	 * Instantiate a new Status object with the first event populated
	 * 
	 * @param type The information type {@link InfoType}
	 * @param eventType The type of event this relates to {@link EventType}
	 * @param code The associated {@link OrchestrationErrorCode} code
	 * @param details Further details on the event
	 */
	public Status(InfoType type, EventType eventType, ErrorCodeInterface code, String details) {
		addEvent(type, eventType, code, details);
	}
	
	/**
	 * Instantiate a new Status object with the first event populated
	 * 
	 * @param type The information type {@link InfoType}
	 * @param eventType The type of event this relates to {@link EventType}
	 * @param code The associated {@link OrchestrationErrorCode} code
	 * @param e The exception
	 */
	public Status(InfoType type, EventType eventType, ErrorCodeInterface code, Exception e) {
		addEvent(type, eventType, code, e.getMessage());
	}
	
	/**
	 * Instantiate a new Status object with the first event populated
	 * 
	 * @param type The information type {@link InfoType}
	 * @param eventType The type of event this relates to {@link EventType}
	 * @param code The associated {@link OrchestrationErrorCode} code
	 * @param message A short message if different to the message attached to the {@link OrchestrationErrorCode}
	 * @param details Further details on the event
	 */
	public Status(InfoType type, EventType eventType, ErrorCodeInterface code, String message, String details) {
		addEvent(type, eventType, code, message, details);
	}
	
	/**
	 * Add an event to the status. This will also raise the status {@link InfoType} to the 
	 * highest level (ordered as OK, WARNING then ERROR). 
	 * 
	 * @param type The information type {@link InfoType}
	 * @param eventType The type of event this relates to {@link EventType}
	 * @param code The associated {@link OrchestrationErrorCode} code
	 * @param details Further details on the event
	 */
	public void addEvent(InfoType type, EventType eventType, ErrorCodeInterface code, String details) {
		addEvent(type, eventType, code, code.getDescription(), details);
	}

	/**
	 * Add an event to the status. This will also raise the status {@link InfoType} to the 
	 * highest level (ordered as OK, WARNING then ERROR). This should only be called if you want 
	 * to override the standard message. 
	 * 
	 * @param type The information type {@link InfoType}
	 * @param eventType The type of event this relates to {@link EventType}
	 * @param code The associated {@link OrchestrationErrorCode} code
	 * @param message A message to summarize the event
	 * @param details Further details on the event
	 */
	public void addEvent(InfoType type, EventType eventType, ErrorCodeInterface code, String message, String details) {
		
		if ((type.equals(InfoType.WARNING) && infoStatus.equals(InfoType.OK))
				|| type.equals(InfoType.ERROR)) {
			infoStatus = type;
		}

		Event e = new Event(type, eventType, code, message, details);
		events.add(e);
		
	}

	/**
	 * Add an event to the status. 
	 * This should only be called if you want to override the standard message. 
	 * 
	 * @param e The event to record {@link Event}
	 */
	public void addEvent(Event e) {
		events.add(e);
	}

	/**
	 * @return the status
	 */
	public InfoType getStatus() {
		return infoStatus;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(InfoType status) {
		this.infoStatus = status;
	}

	/**
	 * @return the events
	 */
	public List<Event> getEvents() {
		return events;
	}

	/**
	 * @param events the events to set
	 */
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	
	

}
