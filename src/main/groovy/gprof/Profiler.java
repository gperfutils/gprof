package gprof;

import groovy.lang.*;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Profiler extends MetaClassRegistry.MetaClassCreationHandle {

    private static class CallInterceptor implements Interceptor {
        private List<Class> includes;
        private List<ProfileCallEntry> callEntries = new ArrayList();
        private long startNanoseconds;

        @Override
        public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
            startNanoseconds = System.nanoTime();
            return null;
        }

        @Override
        public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
            String className;
            if (object.getClass() == Class.class && methodName.equals("ctor")) {
                className = ((Class) object).getName();
            } else {
                className = object.getClass().getName();
            }
            ProfileCallEntry callEntry = new ProfileCallEntry(
                    className, methodName, System.nanoTime() - startNanoseconds);
            callEntries.add(callEntry);
            return result;
        }

        @Override
        public boolean doInvoke() {
            return true;
        }

        public List<ProfileCallEntry> getCallEntries() {
            return callEntries;
        }
    }

    private List<ProfileMetaClass> proxyMetaClasses = new ArrayList();
    private MetaClassRegistry.MetaClassCreationHandle originalMetaClassCreationHandle;
    private CallInterceptor callInterceptor = new CallInterceptor();

    public Profile run(Closure task) {
        return run(Collections.emptyMap(), task);
    }

    public Profile run(Map options, Closure task) {
        start();
        task.call();
        end();
        return new Profile(callInterceptor.getCallEntries());
    }

    private void start() {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
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
    }

    @Override
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        if (theClass != ProfileMetaClass.class) {
            try {
                ProfileMetaClass proxyMetaClass =
                        new ProfileMetaClass(registry, theClass, new MetaClassImpl(registry, theClass));
                proxyMetaClass.setInterceptor(callInterceptor);
                proxyMetaClasses.add(proxyMetaClass);
                return proxyMetaClass;
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        }
        return super.createNormalMetaClass(theClass, registry);
    }


}
