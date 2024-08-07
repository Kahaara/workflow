package au.com.kahaara.wf.orchestration.utils;

import java.lang.reflect.Constructor;

/**
 * @author ashraf
 * from https://examples.javacodegeeks.com/core-java/dynamic-class-loading-example/
 *
 */
public class JavaClassLoader extends ClassLoader {
     
    public Object invokeClassMethod(String classBinName) 
    		throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, Exception {
    	
	    // Create a new JavaClassLoader
	    ClassLoader classLoader = this.getClass().getClassLoader();
	     
	    // Load the target class using its binary name
	    Class<?> loadedMyClass = classLoader.loadClass(classBinName);
	     
	    //System.out.println("Loaded class name: " + loadedMyClass.getName());
	     
	    // Create a new instance from the loaded class
	    Constructor<?> constructor = loadedMyClass.getConstructor();
	    Object myClassObject = constructor.newInstance();
	    
	    return myClassObject;
         
    }
}
	