package au.com.kahaara.wf.orchestration.event;

import au.com.kahaara.wf.orchestration.ErrorCodeInterface;
import au.com.kahaara.wf.orchestration.OrchestrationData;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is where the events are recorded in the database using their own transaction space
 * As each event is written it is also flushed
 * 
 * @author excdsn (Simon Haddon)
 *
 */
@Component
@Transactional
public class EventRecorder implements EventRecorderInterface {

	public static final Logger log = LoggerFactory.getLogger(EventRecorder.class); 	

	public EventRecorder() {
		// Nothing to do
	}


	@Override
	public void recordEvents(OrchestrationData request) {
		try {
			Map<String, Object> data = request.getResultData().size() == 0 ? request.getRequestData() : request.getResultData();
			request.getStatus().getEvents().forEach(e -> saveEvent(data, e));
		} catch (Exception e) {
			log.error("Unable to record request events",e);
		}
	}

	@Override
	public void saveEvent(Map<String, Object> data, Event event) {

		try {
			String message = event.getMessage() + ". " + event.getDetails();
			writeEventMessage(event.getTransactionId(), event.getCode(), message, event.getTimestamp());
		} catch (Exception e) {
			log.error("Unable to write event details", e);
		}

	}


	/**
	 * Part of the event recorder. Save the event or write to the log file. Or both
	 *
	 * @param transactionId The transaction id
	 * @param messageCode The error or message code that goes with this message
	 * @param message The message to write
	 * @param timestamp The timestamp of the event
	 */
	private void writeEventMessage(Long transactionId, ErrorCodeInterface messageCode, String message, Date timestamp) {

		log.error("Event: {} {} {} {}", transactionId, messageCode, message, timestamp);

	}

}
