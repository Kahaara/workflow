package au.com.kahaara.wf.orchestration.rules;

import au.com.kahaara.wf.orchestration.cache.GenericCache;
import au.com.kahaara.wf.orchestration.cache.RuleSetCacheService;
import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.exception.WorkflowException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class RuleSetServiceTest {

    static RuleSetCacheService rss ;

    @Mock
    private static ApplicationContext context;

    @BeforeAll
    static void setup() throws WorkflowException {
        Environment properties = new MockEnvironment();
        RuleList ruleList = new RuleList(properties, context);
        GenericCache<String, RuleSet> cache = new GenericCache<>();
        rss = new RuleSetCacheService(ruleList, cache);
    }

    @Test
    void getRuleList() {

        try {
            rss.getRuleSet("test");
            fail("test rule should not exist");
        } catch (RulesException e) {
        }

    }


}