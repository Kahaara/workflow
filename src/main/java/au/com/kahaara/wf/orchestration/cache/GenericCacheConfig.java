package au.com.kahaara.wf.orchestration.cache;

import au.com.kahaara.wf.OrchestrationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Used the interface so the bean can be instantiated anywhere as required. Obviously, respecting the principles of
 * inversion of control and dependency injection, this occurs outside the Service class.
 * <p>SpringBoot makes this task easier for us</p>
 * <p>the getCache method is able to instantiate an GenericCacheInterface as a dependency for any application
 * class. Not just the GenericCacheInterface<String, RuleSet>. Any cache with any type of key and any type of value!
 * The cache timeout is also injected by Spring itself. In our case it is being defined in the application properties
 * from the property <pre>orchestration.workflow.cache.timeout</pre></p>
 */
@Configuration
public class GenericCacheConfig {

    @Autowired
    private Environment properties;

    @Bean
    public <K, V> GenericCacheInterface<K, V> getCache(@Value("${"+ OrchestrationConfig.PROPERTIES_TIMEOUT +"}") Long cacheTimeout) {
        return new GenericCache<>(cacheTimeout);
    }

}
