/*
 * Copyright 2013 Masato Nagai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gprof;

import groovy.lang.*;
import org.codehaus.groovy.reflection.ClassInfo;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * <p>
 * The profiler. This class provides the following profiling patterns.
 * </p>
 * run() with a callable object:
 * <pre>
 * def prof = new Profiler()
 * prof.run {
 *     // code
 * }.prettyPrint()    
 * </pre>
 * start() and stop():
 * <pre>
 * prof.start()    
 * // code
 * prof.stop()
 * prof.report.prettyPrint()
 * </pre>
 * 
 * @author Masato Nagai
 */
public class Profiler extends MetaClassRegistry.MetaClassCreationHandle {

    private static Map defaultOptions;
    static {
        defaultOptions = new HashMap();
        defaultOptions.put("includeMethods", Collections.emptyList());
        defaultOptions.put("excludeMethods", Collections.emptyList());
        defaultOptions.put("includeThreads", Collections.emptyList());
        defaultOptions.put("excludeThreads", Collections.emptyList());
    }

    private List<ProfileMetaClass> proxyMetaClasses = new ArrayList();
    private MetaClassRegistry.MetaClassCreationHandle originalMetaClassCreationHandle;
    private CallInterceptor interceptor;

    public Report run(Callable profiled) {
        return run(Collections.<String, Object>emptyMap(), profiled);
    }

    /**
     * Runs the specified closure and profiles it.
     * @param options
     *      <ul>
     *      <li>includeMethods a method name to be included.</li>
     *      <li>excludeMethods a method name to be excluded.</li>
     *      <li>includeThreads a thread name to be included.</li>
     *      <li>excludeThreads a thread name to be excluded.</li>
     *      </ul>
     * @param profiled
     *      a callable object to be run and profiled.
     * @return the report
     */
    public Report run(Map<String, Object> options, Callable profiled) {
        try {
            start(options);
            try {
                profiled.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            stop();
            return getReport();
        } finally {
            reset();
        }
    }

    /**
     * Starts profiling.
     */
    public void start() {
        start(Collections.<String, Object>emptyMap());
    }

    /**
     * Starts profiling with the specified options.
     * @param options
     *      <ul>
     *      <li>includeMethods a method name to be included.</li>
     *      <li>excludeMethods a method name to be excluded.</li>
     *      <li>includeThreads a thread name to be included.</li>
     *      <li>excludeThreads a thread name to be excluded.</li>
     *      </ul>
     */
    public void start(Map<String, Object> options) {
        MethodCallFilter methodFilter = new MethodCallFilter();
        ThreadRunFilter threadFilter = new ThreadRunFilter();
        Map<String, Object> opts = new HashMap(defaultOptions);
        opts.putAll(options);
        methodFilter.addIncludes((List) opts.get("includeMethods"));
        methodFilter.addExcludes((List) opts.get("excludeMethods"));
        threadFilter.addIncludes((List) opts.get("includeThreads"));
        threadFilter.addExcludes((List) opts.get("excludeThreads"));
        
        if (interceptor == null) {
            this.interceptor = new CallInterceptor(methodFilter, threadFilter);
        }

        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        
        /*
        for (ClassInfo classInfo : ClassInfo.getAllClassInfo()) {
            Class theClass = classInfo.get();
            originalMetaClasses.put(theClass, registry.getMetaClass(theClass));
            registry.removeMetaClass(theClass);
        }
        */
        // This is a hack.
        Map<Class, MetaClass> originalMetaClasses = new HashMap();
        try {
            Field classSetField = ClassInfo.class.getDeclaredField("globalClassSet");
            classSetField.setAccessible(true);
            ClassInfo.ClassInfoSet classSet = (ClassInfo.ClassInfoSet) classSetField.get(ClassInfo.class);
            for (ClassInfo classInfo : (Collection<ClassInfo>) classSet.values()) {
                Class theClass = classInfo.get();
                MetaClass originalMetaClass = registry.getMetaClass(theClass);
                originalMetaClasses.put(theClass, originalMetaClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        originalMetaClassCreationHandle = registry.getMetaClassCreationHandler();
        registry.setMetaClassCreationHandle(this);
        
        // creates and sets meta classes from the original meta classes
        for (Map.Entry<Class, MetaClass> e : originalMetaClasses.entrySet()) {
            Class theClass = e.getKey();
            MetaClass metaClass = e.getValue();
            MetaClass proxyMetaClass = proxyMetaClass(registry, theClass, metaClass);
            registry.setMetaClass(theClass, proxyMetaClass); 
        }
    }

    /**
     * Stops profiling.
     */
    public void stop() {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        registry.setMetaClassCreationHandle(originalMetaClassCreationHandle);

        for (ProfileMetaClass proxyMetaClass : proxyMetaClasses) {
            Class theClass = proxyMetaClass.getTheClass();
            MetaClass pureMetaClass = proxyMetaClass.getAdaptee();
            registry.setMetaClass(theClass, pureMetaClass);
        }
    }

    /**
     * Resets profiling.
     */
    public void reset() {
        interceptor = null;
    }

    /**
     * Returns the report.
     * @return the report
     */
    public Report getReport() {
        return new ProxyReport(interceptor.getTree());
    }
    
    private MetaClass proxyMetaClass(
            MetaClassRegistry registry, Class theClass, MetaClass metaClass) {
        if (MetaClass.class.isAssignableFrom(theClass)) {
            return metaClass;
        }
        ProfileMetaClass proxyMetaClass = new ProfileMetaClass(registry, theClass, metaClass);
        proxyMetaClass.setInterceptor(interceptor);
        proxyMetaClasses.add(proxyMetaClass);
        return proxyMetaClass;
    }

    @Override
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        if (theClass == ProfileMetaClass.class) {
            return super.createNormalMetaClass(theClass, registry);
        }
        MetaClass metaClass = new MetaClassImpl(registry, theClass);
        return proxyMetaClass(registry, theClass, metaClass);
    }

}
