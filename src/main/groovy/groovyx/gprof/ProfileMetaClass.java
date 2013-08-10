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

import java.beans.IntrospectionException;

public class ProfileMetaClass extends MetaClassImpl implements AdaptingMetaClass {

    protected MetaClass adaptee = null;
    protected CallInterceptor interceptor = null;

    public ProfileMetaClass(MetaClassRegistry registry, Class theClass, MetaClass adaptee) throws IntrospectionException {
        super(registry, theClass);
        this.adaptee = adaptee;
        super.initialize();
    }

    public synchronized void initialize() {
        this.adaptee.initialize();
    }

    public CallInterceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(CallInterceptor interceptor) {
        this.interceptor = interceptor;
    }
    
    private long time() {
        return System.nanoTime();
    }
    
    private long measureOverhead() {
        long s = time();
        return time() - s;
    }
    
    private long elapsedTime(long from) {
        return time() - from;
    }

    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        long interceptStartTime = time();
        long measureOverheadTime = measureOverhead();
        MethodCallInfo methodCall = new MethodCallInfo(object.getClass().getName(), methodName);
        interceptor.beforeInvoke(methodCall);
        long executeStartTime = time();
        try {
            return adaptee.invokeMethod(object, methodName, arguments);
        } finally {
            long executeTime = elapsedTime(executeStartTime) - measureOverheadTime;
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
            return adaptee.invokeStaticMethod(object, methodName, arguments);
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
            return adaptee.invokeConstructor(arguments);
        } finally {
            long executeTime = elapsedTime(executeStartTime);
            methodCall.setTime(executeTime);
            interceptor.afterInvoke(methodCall);
            long interceptTime = elapsedTime(interceptStartTime);
            methodCall.setOverheadTime(interceptTime - executeTime);
        }
    }

    public MetaClass getAdaptee() {
        return this.adaptee;
    }

    public void setAdaptee(MetaClass metaClass) {
        this.adaptee = metaClass;
    }

}
