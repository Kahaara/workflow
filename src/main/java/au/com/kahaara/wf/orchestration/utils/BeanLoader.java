package au.com.kahaara.wf.orchestration.utils;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;

/**
 * See <a href="https://gopinathb4u.wordpress.com/2010/12/30/create-dynamic-spring-beans/">create dynamic spring beans</a>
 * 
 * @author Simon Haddon
 *
 */
public class BeanLoader {

	 static ApplicationContext appContext = null;

	 private BeanLoader() {

	 }

	public static void createDynamicBean(Object bean, ApplicationContext context) {
		AutowireCapableBeanFactory factory = null;
		appContext = context;
		String beanName = bean.getClass().getName();
		factory = appContext.getAutowireCapableBeanFactory();
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(bean.getClass());
		beanDefinition.setAutowireCandidate(true);
		registry.registerBeanDefinition(beanName, beanDefinition);
		factory.autowireBeanProperties(bean,
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
	}

}
