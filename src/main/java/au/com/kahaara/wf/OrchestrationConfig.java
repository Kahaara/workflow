package au.com.kahaara.wf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Set the defaults for orchestration from the orchestration.properties and define the required constants for accessing
 * them. Also define the start end tags.
 *
 */
@Configuration
@PropertySource(ignoreResourceNotFound = false, value = "classpath:orchestration.properties")
public class OrchestrationConfig {

    @Autowired
    Environment properties;

    public static final String PROPERTIES_BASE_PATH="orchestration.workflow";
    public static final String PROPERTIES_PACKAGES=PROPERTIES_BASE_PATH+".rules.packages";
    public static final String PROPERTIES_TIMEOUT=PROPERTIES_BASE_PATH+".cache.timeout";

    public static final String WORKFLOW_START = "START";
    public static final String WORKFLOW_END = "END";

    @Value("${orchestration.data.deepcopy}")
    private boolean deepCopyAllowed ;

    @Value("${orchestration.data.ruleinfo.report}")
    private boolean ruleinfoReport;

    @Value("${orchestration.data.ruleinfo.enforce}")
    private boolean ruleinfoEnforce;

    @Value("${orchestration.data.ruleinfo.preruletest}")
    private boolean ruleRunPreTest;

    public boolean isDeepCopyAllowed() {
        return deepCopyAllowed;
    }

    public boolean isRuleInfoReported() {
        return ruleinfoReport;
    }

    public boolean isRuleInfoEnforce() {
        return ruleinfoEnforce;
    }

    public boolean isRuneRulePreTest() {
        return ruleRunPreTest;
    }


    public void setRuleInfoReported(boolean b) {
        this.ruleinfoReport = b;
    }
    public void setRuleInfoEnforce(boolean b) {
        this.ruleinfoEnforce = b;
    }
}
