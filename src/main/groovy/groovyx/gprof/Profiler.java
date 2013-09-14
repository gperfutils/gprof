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
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.lang.reflect.Field;
import java.util.*;

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
        defaultOptions.put("excludeMethods", Arrays.asList(Profiler.class.getName() + ".stop"));
        defaultOptions.put("includeThreads", Collections.emptyList());
        defaultOptions.put("excludeThreads", Collections.emptyList());
    }

    private MetaClassRegistry.MetaClassCreationHandle originalMetaClassCreationHandle;
    private CallInterceptor interceptor;

    public Report run(Closure profiled) {
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
    public Report run(Map<String, Object> options, Closure profiled) {
        try {
            List refs = new ArrayList();
            try {
                for (Field field : profiled.getClass().getDeclaredFields()) {
                    if (field.getType().equals(Reference.class)) {
                        field.setAccessible(true);
                        Reference ref = (Reference) field.get(profiled);
                        refs.add(ref);
                    }
                }
            } catch(Exception e) {}
            refs.add(new Reference(profiled));
            refs.add(new Reference(profiled.getDelegate()));
            Map<String, Object> _options = new HashMap(options);
            _options.put("references", refs);
            options = null;
            start(_options);
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

        proxyMetaClasses();
        List<Reference> refs = (List) opts.get("references");
        if (refs != null) {
            proxyPerInstanceMetaClasses(refs);
        }
    }

    private void proxyPerInstanceMetaClasses(List<Reference> refs) {
        for (Reference ref : refs) {
            Object obj = ref.get();
            if (obj != null) {
                Class theClass = obj.getClass();
                if (obj instanceof GroovyObject) {
                    GroovyObject gobj = (GroovyObject) obj;
                    MetaClass metaClass = gobj.getMetaClass();
                    MetaClass proxyMetaClass = proxyMetaClass(theClass, metaClass);
                    DefaultGroovyMethods.setMetaClass(gobj, proxyMetaClass);
                } else {
                    MetaClass metaClass = DefaultGroovyMethods.getMetaClass(obj);
                    MetaClass proxyMetaClass = proxyMetaClass(theClass, metaClass);
                    MetaClassHelper.doSetMetaClass(obj, proxyMetaClass);
                }
            }
        }
    }

    private void proxyMetaClasses() {
        Set<Class> allClasses = getLoadedClasses();
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        Map<Class, MetaClass> originalMetaClasses = new HashMap();
        for (Class theClass : allClasses) {
            MetaClass originalMetaClass = registry.getMetaClass(theClass);
            originalMetaClasses.put(theClass, originalMetaClass);
        }

        originalMetaClassCreationHandle = registry.getMetaClassCreationHandler();
        registry.setMetaClassCreationHandle(this);

        // creates and sets meta classes from the original meta classes
        for (Map.Entry<Class, MetaClass> e : originalMetaClasses.entrySet()) {
            Class theClass = e.getKey();
            MetaClass metaClass = e.getValue();
            MetaClass proxyMetaClass = proxyMetaClass(theClass, metaClass);
            registry.setMetaClass(theClass, proxyMetaClass); 
        }
    }

    private Set<Class> getLoadedClasses() {
        Set<Class> allClasses = new HashSet();
        /*
        for (ClassInfo classInfo : ClassInfo.getAllClassInfo()) {
            Class theClass = classInfo.get();
            if (theClass.equals(String.class)) {
                System.out.println("found String metaClass: " + registry.getMetaClass(theClass));
            }
            originalMetaClasses.put(theClass, registry.getMetaClass(theClass));
        }
        */
        // ClassInfo.getAllClassInfo() returns cached class info and there is a case it doesn't return
        // all of the loaded classes. This is a hack to ignore the cache.
        try {
            Field classSetField = ClassInfo.class.getDeclaredField("globalClassSet");
            classSetField.setAccessible(true);
            ClassInfo.ClassInfoSet classSet = (ClassInfo.ClassInfoSet) classSetField.get(ClassInfo.class);
            for (ClassInfo classInfo : (Collection<ClassInfo>) classSet.values()) {
                allClasses.add(classInfo.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allClasses;
    }

    /**
     * Stops profiling.
     */
    public void stop() {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        Set<ProfileMetaClass> proxyMetaClasses = new HashSet(); 
        for (ClassInfo classInfo : ClassInfo.getAllClassInfo()) {
            Class theClass = classInfo.get();
            MetaClass metaClass = registry.getMetaClass(theClass);
            if (metaClass instanceof ProfileMetaClass) {
                proxyMetaClasses.add((ProfileMetaClass) metaClass);
            }
        }
        
        // resetting the meta class creation handler clears all the meta classes.
        registry.setMetaClassCreationHandle(originalMetaClassCreationHandle);

        for (ProfileMetaClass proxyMetaClass : proxyMetaClasses) {
            registry.setMetaClass(proxyMetaClass.getTheClass(), proxyMetaClass.getAdaptee());
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
    
    private MetaClass proxyMetaClass(Class theClass, MetaClass metaClass) {
        if (MetaClass.class.isAssignableFrom(theClass)) {
            return metaClass;
        }
        ProfileMetaClass proxyMetaClass = new ProfileMetaClass(theClass, metaClass);
        proxyMetaClass.setInterceptor(interceptor);
        return proxyMetaClass;
    }

    @Override
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        if (theClass == ProfileMetaClass.class) {
            return super.createNormalMetaClass(theClass, registry);
        }
        MetaClass metaClass = new MetaClassImpl(registry, theClass);
        return proxyMetaClass(theClass, metaClass);
    }

}
