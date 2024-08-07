package au.com.kahaara.wf.orchestration;

/**
 * There are 2 modes that Orchestration can run. Int the normal mode it performs the orchestration
 * as expected but in test mode it runs the rules without actually doing anything except calling getInfo
 * and logging the details
 */
public enum OrchestrationMode {
  NORMAL,
  TEST
}

