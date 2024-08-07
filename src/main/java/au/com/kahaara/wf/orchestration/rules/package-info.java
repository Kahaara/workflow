/**
 * This is where the main interfaces to individual rules reside.
 * <p>When the orchestration initializes it examines the packages listed in the packages to search
 * for any potential rules and adds them to the list of available rules.</p>
 * <p>It provides the following main functionality</p>
 * <ol>
 *     <li>Loading of the available rules at startup</li>
 *     <li>Caching of rule sets</li>
 *     <li>Processing of rules</li>
 * </ol>
 * 
 */
package au.com.kahaara.wf.orchestration.rules;