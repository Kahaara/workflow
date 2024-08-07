package au.com.kahaara.wf.orchestration;

/**
 * Is this a warning or error. The default state is OK which indicates that the workflow
 * can continue in the assembly stage
 */
public enum InfoType {
  OK,
  WARNING, 
  ERROR
}

