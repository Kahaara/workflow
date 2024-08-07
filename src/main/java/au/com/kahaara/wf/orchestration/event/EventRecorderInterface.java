package au.com.kahaara.wf.orchestration.event;

import au.com.kahaara.wf.orchestration.OrchestrationData;

import java.util.Map;


/**
 * This is where the events are recorded in the database using their own transaction space
 * As each event is written it is also flushed
 * 
 * @author excdsn (Simon Haddon)
 *
 */
public interface EventRecorderInterface {

	/**
	 * This is the event recorder service. It is also in it's own transactional state
	 * so that these events get written no matter the outcome from the orchestration
	 *
	 * @param request The status details from the mapping stage
	 */
	void recordEvents(OrchestrationData request);

	/**
	 * Save the event to the log file
	 *
	 * @param data The {@link Map} data object consisting of classname, data
	 * @param event The event to save
	 */
	void saveEvent(Map<String, Object> data, Event event);
}
