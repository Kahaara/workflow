package au.com.kahaara.wf.orchestration.event;

/**
 * What type of event is this. Currently, events only apply to
 * <ul>
 *   <li>{@link #RULE}
 *   <li>{@link #EXCEPTION}
 * </ul>
 * 
 * @author excdsn
 *
 */
public enum EventType {
	
	//TODO: Move these out of here like I did for error codes
	
	/**
	 * Events relating to a rule processing type event
	 */
	RULE,
	/**
	 * General processing exceptions that we didn't capture correctly
	 */
	EXCEPTION

}
