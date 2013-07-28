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

import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassRegistry;
import org.codehaus.groovy.reflection.ClassInfo;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;

public class Profiler extends MetaClassRegistry.MetaClassCreationHandle {

    private static Map defaultOptions;
    static {
        defaultOptions = new HashMap();
        defaultOptions.put("includeMethods", Collections.emptyList());
        defaultOptions.put("excludeMethods", Arrays.asList("groovyx.gprof.*"));
        defaultOptions.put("includeThreads", Collections.emptyList());
        defaultOptions.put("excludeThreads", Collections.emptyList());
    }

    private List<ProxyMetaClass> proxyMetaClasses = new ArrayList();
    private MetaClassRegistry.MetaClassCreationHandle originalMetaClassCreationHandle;
    private List<MetaClass> originalMetaClasses = new ArrayList();
    private CallInterceptor interceptor;

    public Report run(Callable profiled) {
        return run(Collections.<String, Object>emptyMap(), profiled);
    }

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

    public void start() {
        start(Collections.<String, Object>emptyMap());
    }

    public void start(Map<String, Object> options) {
        Map<String, Object> opts = new HashMap(defaultOptions);
        opts.putAll(options);
        MethodCallFilter methodFilter = new MethodCallFilter();
        methodFilter.addIncludes((List) opts.get("includeMethods"));
        methodFilter.addExcludes((List) opts.get("excludeMethods"));
        ThreadRunFilter threadFilter = new ThreadRunFilter();
        threadFilter.addIncludes((List) opts.get("includeThreads"));
        threadFilter.addExcludes((List) opts.get("excludeThreads"));
        if (interceptor == null) {
            this.interceptor = new CallInterceptor(methodFilter, threadFilter);
        }

        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        originalMetaClassCreationHandle = registry.getMetaClassCreationHandler();
        registry.setMetaClassCreationHandle(this);
        /*
        for (ClassInfo classInfo : ClassInfo.getAllClassInfo()) {
            Class theClass = classInfo.get();
            originalMetaClasses.add(registry.getMetaClass(theClass));
            registry.removeMetaClass(theClass);
        }
        */
        // This is a hack.
        try {
            Field classSetField = ClassInfo.class.getDeclaredField("globalClassSet");
            classSetField.setAccessible(true);
            ClassInfo.ClassInfoSet classSet = (ClassInfo.ClassInfoSet) classSetField.get(ClassInfo.class);
            for (ClassInfo classInfo : (Collection<ClassInfo>) classSet.values()) {
                Class theClass = classInfo.get();
                originalMetaClasses.add(registry.getMetaClass(theClass));
                registry.removeMetaClass(theClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        registry.setMetaClassCreationHandle(originalMetaClassCreationHandle);
        for (ProxyMetaClass metaClass : proxyMetaClasses) {
            // clean registry and delegate creating normal meta class for original handle
            registry.removeMetaClass(metaClass.getTheClass());
        }
        for (MetaClass metaClass : originalMetaClasses) {
            registry.setMetaClass(metaClass.getTheClass(), metaClass);
        }
    }

    public void reset() {
        interceptor = null;
    }

    public Report getReport() {
        return new ProxyReport(interceptor.getTree());
    }

    @Override
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        if (theClass != ProxyMetaClass.class) {
            try {
                ProxyMetaClass proxyMetaClass =
                        new ProxyMetaClass(registry, theClass, new MetaClassImpl(registry, theClass));
                proxyMetaClass.setInterceptor(interceptor);
                proxyMetaClasses.add(proxyMetaClass);
                return proxyMetaClass;
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        }
        return super.createNormalMetaClass(theClass, registry);
    }

}
