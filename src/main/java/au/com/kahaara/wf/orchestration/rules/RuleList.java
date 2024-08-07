package au.com.kahaara.wf.orchestration.rules;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import au.com.kahaara.wf.OrchestrationConfig;
import au.com.kahaara.wf.orchestration.exception.WorkflowException;
import au.com.kahaara.wf.orchestration.utils.BeanLoader;
import au.com.kahaara.wf.orchestration.utils.JavaClassLoader;
import au.com.kahaara.wf.orchestration.workflow.Workflow;
import au.com.kahaara.wf.orchestration.workflow.WorkflowMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;


/**
 * This bean performs a component scan for any components annotated as {@link Workflow}
 * and adds them the list of allowable classed to execute as part of the rules processing.
 * When loading rules into the list of allowable rules if a duplicate rule is defined then
 * this  class will throw an {@link WorkflowException} and will result in the application
 * service shutting down.
 * 
 * @author excdsn
 *
 */
@Component
public class RuleList {
	
	public static final Logger log = LoggerFactory.getLogger(RuleList.class);

	/**
	 * The maximum number of properties allowed for the location of workflow packages.
	 */
	private static final int MAX_EXTERNAL_PROPERTIES = 100;

	/**
	 * In here as a constant but this might be moved to config
	 */
	private static final boolean THROW_EXCEPTION_ON_DUPLICATE = true;

	/**
	 * Initialized via constructor
	 */
	private final Environment properties;

	/**
	 * Initialized via constructor
	 */
	private final ApplicationContext context;

	/**
	 * Provides a list of paths that need examining for available packages
	 * that can contain rules
	 */
	private final List<String> packages = new ArrayList<>();

	/**
	 * The map of rules along with the associated initialized objects.
	 * The String is the rule name as uppercase.
	 */
	private final Map<String, RuleType> rules = new HashMap<>();

	/**
	 * The classpath scanner to help find the available rules that are already defined as beans
	 */
	ClassPathScanningCandidateComponentProvider scanner;

	/**
	 * The bean constructor used by spring-boot
	 *
	 * @param properties The application environment properties
	 * @param context The application context
	 * @throws WorkflowException if there is a problem loading any of the rules
	 */
	public RuleList(Environment properties, ApplicationContext context) throws WorkflowException {
		this.properties = properties;
		this.context = context;
		this.init();
	}

	/**
	 * @return the ruleClasses
	 */
	public Map<String, RuleType> getRuleList() {
		return rules;
	}

	/**
	 * Perform any post construction activities
	 *
	 * @throws WorkflowException If the rules cannot be created
	 */
	private void init() throws WorkflowException {
		
		scanner = new ClassPathScanningCandidateComponentProvider(true);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Workflow.class));
		setInternalPackagePath();
		setRulePackagesPath();
		loadRuleClasses();

	}
	
	/**
	 * Create the initial package search path to access our own workflow
	 * rules. This is basically the start and end rule.
	 */
	private void setInternalPackagePath() {
		String s = this.getClass().getPackage().getName();
		s = s+".def";  // Defaults package
		packages.add(s);
		
	}

	/**
	 * Create the list of packages that can contain rules
	 */
	private void setRulePackagesPath() {
		for (int i = 0 ; i <= MAX_EXTERNAL_PROPERTIES; i++) {
			String p2 = properties.getProperty(OrchestrationConfig.PROPERTIES_PACKAGES+"["+i+"]");
			if (p2 == null) {
				break;
			}
			packages.add(p2);
		}
		
	}

	/**
	 * Go through each package in the list and find any classes listed in the defined path
	 * list searching for any beans or classes and loading them into this object ready for
	 * rule processing
	 *
	 * @throws WorkflowException Unable to load and create the rules objects
	 */
	private void loadRuleClasses() throws WorkflowException {
		
		log.info("Loading orchestration workflow rules");
		for (String path : packages) {
			loadInPackage(path);
		}
	}

	/**
	 * Load any workflow classed from the package path
	 *
	 * @param path THe package path to examine.
	 * @throws WorkflowException If there is a problem
	 */
	private void loadInPackage(String path) throws WorkflowException {

		log.debug("Examining classes in {}",path);
		Set<BeanDefinition> dbList = scanner.findCandidateComponents(path);

		for ( BeanDefinition bd : dbList) {

			String className = bd.getBeanClassName();

			Object o = getWorkflowBean(className);
			// We have a workflow bean
			String ruleName = getAnnotatedRuleName(o);
			if (ruleName != null) {

				Map<String, RuleType> methods = getRuleMethods(o, ruleName);
				for (Map.Entry<String, RuleType> entry : methods.entrySet()) {
					if (rules.containsKey(entry.getKey().toUpperCase())) {
						if (THROW_EXCEPTION_ON_DUPLICATE) {
							throw new WorkflowException("Rule "+entry.getKey()+" from class "+entry.getValue().getRuleClass()+" duplicate of rule in class "+rules.get(entry.getKey().toUpperCase()).getRuleClass());
						}
						log.warn("Replacing {}, Rule {} with {}, Rule {}", rules.get(entry.getKey().toUpperCase()).getRuleClass(), entry.getKey(), bd.getBeanClassName(), entry.getKey());
					} else {
						log.info("Adding {}, Rule {}", bd.getBeanClassName(), entry.getKey());
					}
					if (rules.containsKey(entry.getKey().toUpperCase())) {
					}
					// Store the map with uppercase names only
					rules.put(entry.getKey().toUpperCase(), entry.getValue());

				}
			}

		}
	}

	/**
	 * Get a workflow bean from springboot if already loaded otherwise create
	 * it and register it with spring-boot.
	 * 
	 * @param className The FQN of the class
	 * @return The object bean
	 * @throws WorkflowException If unable to find the class
	 */
	private Object getWorkflowBean(String className) throws WorkflowException  {
		
		Object o = null;

		try {

			o = loadBeanClass(className);
			if (o != null) {
				return o;
			} else {
				// Not loaded yet so lets do it ourselves.
				o = new JavaClassLoader().invokeClassMethod(className);
				if (workflowAnnotation(o)) {
					BeanLoader.createDynamicBean(o, context);
				}
			}

		} catch (ClassNotFoundException e) {
			log.error("Class {} not found", className);
	        throw new WorkflowException("Class not found "+className);
		} catch (Exception e) {
			// this exception is from trying to make it a bean . Still use it however
			log.warn("Class {} cannot be loaded as a bean. Still loading into rule list", className);
		}
		return o;
	}

	/**
	 * Load the class if we can.
	 * Seeing if the class is available as a bean and already loaded
	 * @param className The FQN of the class
	 * @return The loaded class
	 */
	private Object loadBeanClass(String className) throws ClassNotFoundException {

		Object o;
		ClassLoader classLoader = this.getClass().getClassLoader();

		try {
			Class<?> loadedMyClass = classLoader.loadClass(className);
			o = context.getBean(loadedMyClass);
			if (workflowAnnotation(o)) {
				return o;
			}
		} catch (NoSuchBeanDefinitionException | BeanCreationException e) {
			// Ignore these as the bean may just not exist
			// Ignore these as the bean may just not be a workflow bean
		} catch (NullPointerException e) {
			log.warn("Unable to get bean due to no application context");
		} catch (Exception e) {
			//Ignore but log
			log.warn("Unable to get bean due to {}. This would likely be due to a missing ApplicationContext", e.getMessage());
		}
		return null;
	}

	/**
	 * Interact with the {@link Workflow} annotation and get the rule name or
	 * get the class name as the rule name if otherwise not defined.
	 * 
	 * @param o The annotated object
	 * @return The rule name as a string
	 */
	private String getAnnotatedRuleName(Object o) {

		if (o == null) {
			return null;
		}

		Workflow[] a = o.getClass().getAnnotationsByType(Workflow.class);
		if (a.length > 0 && !a[0].rulename().isEmpty()) {
			// There should be only one
			return a[0].rulename();
		} else {
			// return the classname as the rule minus the tail "Rule" string if present
			String s = o.getClass().getSimpleName();
			if (s.endsWith("Rule")) {
				s = s.substring(0, s.length()-4);
			}
			return s;
			
		}
		
	}

	/**
	 * Get the rule methods to invoke at runtime. This allows for more than one rule
	 * in a class.
	 *
	 * @param o The object to examine
	 * @param classRuleName The rulename from the Workflow annotation
	 * @return A map of either a single rule or multiple rules.
	 */
	private Map<String, RuleType> getRuleMethods(Object o, String classRuleName) {

		Class<?> clazz = o.getClass();
		Map<String, RuleType> methodMap = new HashMap<>();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(WorkflowMethod.class)) {
				method.setAccessible(true);
				WorkflowMethod[] keys = method.getAnnotationsByType(WorkflowMethod.class);
				String key = keys[0].rulename();
				RuleType rt = new RuleType(key, o, method);
				methodMap.put(key, rt);
			} else if (method.getName().equals("runRule")) {
				RuleType rt = new RuleType(classRuleName, o, method);
				methodMap.put(classRuleName, rt);
			}
		}

		return methodMap;
	}

	private boolean workflowAnnotation(Object object) {
		if (Objects.isNull(object)) {
			return false;
	    }
	        
	    Class<?> clazz = object.getClass();
		return clazz.isAnnotationPresent(Workflow.class);
	}


}
