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
package gprof;

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
        // I believe grabbing phase must be excluded. Is there anyone wants to profile it?
        defaultOptions.put("excludeMethods", Arrays.asList("groovy.grape.*"));
        defaultOptions.put("includeThreads", Collections.emptyList());
        defaultOptions.put("excludeThreads", Collections.emptyList());
    }

    private List<ProfileMetaClass> proxyMetaClasses = new ArrayList();
    private MetaClassRegistry.MetaClassCreationHandle originalMetaClassCreationHandle;
    private List<MetaClass> originalMetaClasses = new ArrayList();
    private ProfileInterceptor interceptor;

    public Profile run(Callable profiled) {
        return run(Collections.<String, Object>emptyMap(), profiled);
    }

    public Profile run(Map<String, Object> options, Callable profiled) {
        Map<String, Object> opts = new HashMap(defaultOptions);
        opts.putAll(options);

        ProfileMethodFilter methodFilter = new ProfileMethodFilter();
        methodFilter.addIncludes((List) opts.get("includeMethods"));
        methodFilter.addExcludes((List) opts.get("excludeMethods"));
        ProfileThreadFilter threadFilter = new ProfileThreadFilter();
        threadFilter.addIncludes((List) opts.get("includeThreads"));
        threadFilter.addExcludes((List) opts.get("excludeThreads"));
        this.interceptor = new ProfileInterceptor(methodFilter, threadFilter);

        start();
        try {
            profiled.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        end();
        return new Profile(interceptor.getTree());
    }

    private void start() {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();

        for (ClassInfo classInfo : ClassInfo.getAllClassInfo()) {
            Class theClass = classInfo.get();
            originalMetaClasses.add(registry.getMetaClass(theClass));
            registry.removeMetaClass(theClass);
        }
        // This is a hack.
        // Remove classes that have not been ClassInfo
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            while (loader != null) {
                Field classesField = ClassLoader.class.getDeclaredField("classes");
                classesField.setAccessible(true);
                Vector<Class> classes = (Vector<Class>) classesField.get(loader);
                for (int i = 0, n = classes.size(); i < n; i++) {
                    try {
                        Class theClass = classes.get(i);
                        originalMetaClasses.add(registry.getMetaClass(theClass));
                        registry.removeMetaClass(theClass);
                    } catch (NoClassDefFoundError e) {
                    }
                }
                loader = loader.getParent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        originalMetaClassCreationHandle = registry.getMetaClassCreationHandler();
        registry.setMetaClassCreationHandle(this);
    }

    private void end() {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        registry.setMetaClassCreationHandle(originalMetaClassCreationHandle);
        for (ProfileMetaClass metaClass : proxyMetaClasses) {
            // clean registry and delegate creating normal meta class for original handle
            registry.removeMetaClass(metaClass.getTheClass());
        }
        for (MetaClass metaClass : originalMetaClasses) {
            registry.setMetaClass(metaClass.getTheClass(), metaClass);
        }
    }

    @Override
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        if (theClass != ProfileMetaClass.class) {
            try {
                ProfileMetaClass proxyMetaClass =
                        new ProfileMetaClass(registry, theClass, new MetaClassImpl(registry, theClass));
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
