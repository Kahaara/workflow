package au.com.kahaara.wf.orchestration.cache;

import au.com.kahaara.wf.orchestration.exception.RulesException;
import au.com.kahaara.wf.orchestration.rules.RuleList;
import au.com.kahaara.wf.orchestration.rules.RuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * If a rule set has been previously created then it will be here. Rule sets can be found by the
 * rule list as they key.
 */
@Service
public class RuleSetCacheService {

    public static final Logger log = LoggerFactory.getLogger(RuleSetCacheService.class);

    private final RuleList ruleList;

    private final GenericCache<String, RuleSet> cache;

    /**
     * Create the rules cache as a spring-bean component
     *
     * @param ruleList The rule list as scanned at startup
     * @param cache the cache manager
     */
    public RuleSetCacheService(RuleList ruleList, GenericCache<String, RuleSet> cache) {
        log.info("Initializing service for orchestration cache");
        this.ruleList = ruleList;
        this.cache = cache;
    }

    /**
     * Get a rule set from the cache if present otherwise create it and
     * add to the cache before returning the newly created rule set
     *
     * @param key the cache key which is the name of the rule set
     * @return a rule set from either the cache or newly created
     * @throws RulesException If unable to create the ruleset
     */
    public synchronized RuleSet getRuleSet(String key) throws RulesException {
        Optional<RuleSet> ruleSet = this.cache.get(key);
        if (!ruleSet.isPresent()) {
            ruleSet = this.fromRuleList(key);
        } else {
            log.info("Pulled workflow rules {} from cache", key);
        }
        return ruleSet.orElseGet(RuleSet::new);
    }

    /**
     * Retrieve the {@link RuleSet} from the cache.
     *
     * @param key the cache key which is the name of the rule set
     * @return an {@link Optional} rule set
     * @throws RulesException If unable to create the ruleset
     */
    Optional<RuleSet> fromRuleList(String key) throws RulesException {
        if(this.cache.containsKey(key)){
            return this.cache.get(key);
        }else {
            RuleSet ruleSet = new RuleSet(key, ruleList);
            this.cache.put(key, ruleSet);
            return Optional.of(ruleSet);
        }
    }

}
