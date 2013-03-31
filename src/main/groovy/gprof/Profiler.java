package gprof;

import groovy.lang.*;
import org.codehaus.groovy.reflection.ClassInfo;

import java.beans.IntrospectionException;
import java.util.*;

public class Profiler extends MetaClassRegistry.MetaClassCreationHandle {

    private static class CallInterceptor implements Interceptor {
        private List<Class> includes;
        private ProfileTree<ProfileCallEntry> callTree;
        private ProfileTree.Node<ProfileCallEntry> currentCall;
        private Stack<Long> startTimeStack = new Stack();

        CallInterceptor() {
            currentCall = new ProfileTree.Node(new ProfileCallEntry("", ""));
            callTree = new ProfileTree(currentCall);
        }

        @Override
        public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
            startTimeStack.push(System.nanoTime());
            String className;
            if (object.getClass() == Class.class && methodName.equals("ctor")) {
                className = ((Class) object).getName();
            } else {
                className = object.getClass().getName();
            }
            ProfileCallEntry callEntry = new ProfileCallEntry(className, methodName);
            callEntry.setStartTime(new ProfileTime(System.nanoTime()));
            ProfileTree.Node<ProfileCallEntry> theCall = new ProfileTree.Node(callEntry);
            currentCall.addChild(theCall);
            theCall.setParent(currentCall);
            currentCall = theCall;
            return null;
        }

        @Override
        public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
            currentCall.getData().setEndTime(new ProfileTime(System.nanoTime()));
            currentCall = currentCall.getParent();
            return result;
        }

        @Override
        public boolean doInvoke() {
            return true;
        }

        public ProfileTree getCallTree() {
            return callTree;
        }
    }

    private List<ProfileMetaClass> proxyMetaClasses = new ArrayList();
    private MetaClassRegistry.MetaClassCreationHandle originalMetaClassCreationHandle = null;
    private List<MetaClass> originalMetaClasses = new ArrayList();
    private CallInterceptor callInterceptor = new CallInterceptor();

    public Profile run(Closure task) {
        return run(Collections.emptyMap(), task);
    }

    public Profile run(Map options, Closure task) {
        start();
        task.call();
        end();
        return new Profile(callInterceptor.getCallTree());
    }

    private void start() {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        for (ClassInfo classInfo : ClassInfo.getAllClassInfo()) {
            Class theClass = classInfo.get();
            // MetaClass metaClass = registry.getMetaClass(theClass);
            // registry.setMetaClass(theClass, new ProfileMetaClass(registry, theClass));
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
