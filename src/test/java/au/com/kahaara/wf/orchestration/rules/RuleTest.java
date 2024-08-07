package au.com.kahaara.wf.orchestration.rules;

import au.com.kahaara.wf.orchestration.rules.def.WorkflowEnd;
import au.com.kahaara.wf.orchestration.rules.def.WorkflowStart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RuleTest {

    static Rule rule;
    static WorkflowStart start = new WorkflowStart();
    static WorkflowEnd end = new WorkflowEnd();

    @BeforeAll
    static void setUp() {

        rule = new Rule(new RuleType("test", start));
        rule.setRuleName("test");
        rule.setRuleType(new RuleType("test", start));
        rule.setOnTrue(rule);
        rule.setOnFalse(rule);
    }

    @Test
    void getRuleName() {
        Assertions.assertEquals(rule.getRuleName(),"test");
    }

    @Test
    void getObject() {
        Assertions.assertEquals(start, rule.getRuleType().getRuleClass());
    }

    @Test
    void getOnTrue() {
        Assertions.assertEquals(rule, rule.getOnTrue());
    }

    @Test
    void getOnFalse() {
        Assertions.assertEquals(rule, rule.getOnFalse());
    }
}