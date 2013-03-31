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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Profiler extends MetaClassRegistry.MetaClassCreationHandle {

    private List<ProfileMetaClass> proxyMetaClasses = new ArrayList();
    private MetaClassRegistry.MetaClassCreationHandle originalMetaClassCreationHandle = null;
    private List<MetaClass> originalMetaClasses = new ArrayList();
    private ProfileInterceptor interceptor = new ProfileInterceptor();

    public Profile run(Callable profiled) {
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
