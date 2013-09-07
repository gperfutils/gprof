/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gprof;

import groovy.lang.*;

/**
 * This class profiles when the method of the class is invoked.
 */
public class ProfileMetaClass
    // org.codehaus.groovy.runtime.HandleMetaClass replaces the meta class with a new object of ExpandoMetaClass
    // when a program tries to modify the meta class and the meta class is not an object of ExpandoMetaClass
    // So this class need to extend ExpandoMetaClass and cannot extend ProxyMetaClass.
    extends AdaptingExpandoMetaClass {
    // extends ProxyMetaClass {
    
    protected CallInterceptor interceptor = null;

    public ProfileMetaClass(MetaClassRegistry registry, Class theClass, MetaClass metaClass) {
        super(metaClass, registry, theClass);
        super.initialize();
    }

    public void setInterceptor(CallInterceptor interceptor) {
        this.interceptor = interceptor;
    }
    
    private long time() {
        return System.nanoTime();
    }
    
    private long elapsedTime(long from) {
        return time() - from;
    }

    @Override
    public MetaMethod pickMethod(String methodName, Class[] arguments) {
        MetaMethod metaMethod = super.pickMethod(methodName, arguments);
        if (metaMethod instanceof ClosureInvokingMethod) {
            // replace the meta class of the closure to intercept method calls in it.
            Closure closure = ((ClosureInvokingMethod) metaMethod).getClosure();
            if (!(closure.getMetaClass() instanceof ProfileMetaClass)) {
                ProfileMetaClass proxyMetaClass = 
                        new ProfileMetaClass(this.getRegistry(), closure.getClass(), closure.getMetaClass());
                proxyMetaClass.setInterceptor(interceptor);
                closure.setMetaClass(proxyMetaClass);
            }
        }
        return metaMethod;
    }

    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        long interceptStartTime = time();
        MethodCallInfo methodCall = new MethodCallInfo(object.getClass().getName(), methodName);
        interceptor.beforeInvoke(methodCall);
        long executeStartTime = time();
        try {
            return super.invokeMethod(object, methodName, arguments);
        } finally {
            long executeTime = elapsedTime(executeStartTime);
            methodCall.setTime(executeTime);
            interceptor.afterInvoke(methodCall);
            long interceptTime = elapsedTime(interceptStartTime);
            methodCall.setOverheadTime(interceptTime - executeTime);
        }
    }

    public Object invokeStaticMethod(final Object object, final String methodName, final Object[] arguments) {
        long interceptStartTime = time();
        MethodCallInfo methodCall = new MethodCallInfo(theClass.getName(), methodName);
        interceptor.beforeInvoke(methodCall);
        long executeStartTime = time();
        try {
            return super.invokeStaticMethod(object, methodName, arguments);
        } finally {
            long executeTime = elapsedTime(executeStartTime);
            methodCall.setTime(executeTime);
            interceptor.afterInvoke(methodCall);
            long interceptTime = elapsedTime(interceptStartTime);
            methodCall.setOverheadTime(interceptTime - executeTime);
        }
    }

    public Object invokeConstructor(final Object[] arguments) {
        long interceptStartTime = time();
        MethodCallInfo methodCall = new MethodCallInfo(theClass.getName(), "ctor");
        interceptor.beforeInvoke(methodCall);
        long executeStartTime = time();
        try {
            return super.invokeConstructor(arguments);
        } finally {
            long executeTime = elapsedTime(executeStartTime);
            methodCall.setTime(executeTime);
            interceptor.afterInvoke(methodCall);
            long interceptTime = elapsedTime(interceptStartTime);
            methodCall.setOverheadTime(interceptTime - executeTime);
        }
    }

}
