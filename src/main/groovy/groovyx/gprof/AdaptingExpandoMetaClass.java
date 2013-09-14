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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.MixinInMetaClass;
import org.codehaus.groovy.runtime.callsite.CallSite;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdaptingExpandoMetaClass extends ExpandoMetaClass implements AdaptingMetaClass {
    
    protected MetaClass adaptee;

    public AdaptingExpandoMetaClass(MetaClass metaClass, Class theClass) {
        super(theClass, false, false);
        setAdaptee(metaClass);
    }
    
    private ExpandoMetaClass expandableDelegate() {
        if (!(adaptee instanceof ExpandoMetaClass)) {
            GroovySystem.getMetaClassRegistry().setMetaClass(adaptee.getTheClass(), adaptee);
            adaptee = new ExpandoMetaClass(adaptee.getTheClass(), false, true);    
            adaptee.initialize();
            GroovySystem.getMetaClassRegistry().setMetaClass(adaptee.getTheClass(), this);
        }
        return (ExpandoMetaClass) adaptee;
    }
    
    private MetaClassImpl metaClassImplDelegate() {
        if (!(adaptee instanceof MetaClassImpl)) {
            return expandableDelegate();    
        }
        return (MetaClassImpl) adaptee;
    }

// MetaClass

    @Override
    public void initialize() {
        adaptee.initialize();
    }

    @Override
    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        return adaptee.invokeStaticMethod(object, methodName, arguments);
    }

    @Override
    public Object invokeMethod(Object object, String methodName, Object arguments) {
        return adaptee.invokeMethod(object, methodName, arguments);
    }

    @Override
    public Object invokeMethod(Object object, String methodName, Object[] originalArguments) {
        return adaptee.invokeMethod(object, methodName, originalArguments);
    }

    @Override
    public Object invokeMethod(Class sender, Object object, String methodName, Object[] originalArguments, boolean isCallToSuper, boolean fromInsideClass) {
        return adaptee.invokeMethod(sender, object, methodName, originalArguments, isCallToSuper, fromInsideClass);
    }

    @Override
    public Object invokeMissingMethod(Object instance, String methodName, Object[] arguments) {
        return adaptee.invokeMissingMethod(instance, methodName, arguments);
    }

    @Override
    public Object invokeMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter) {
        return adaptee.invokeMissingProperty(instance, propertyName, optionalValue, isGetter);
    }

    @Override
    public Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper) {
        return adaptee.getAttribute(sender, receiver, messageName, useSuper);
    }

    @Override
    public void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        adaptee.setAttribute(sender, receiver, messageName, messageValue, useSuper, fromInsideClass);
    }

    @Override
    public Object invokeConstructor(Object[] arguments) {
        return adaptee.invokeConstructor(arguments);
    }

    @Override
    public Object getProperty(Object object, String name) {
        return adaptee.getProperty(object, name);
    }
    
    @Override
    public Object getProperty(Class sender, Object object, String name, boolean useSuper, boolean fromInsideClass) {
        return adaptee.getProperty(sender, object, name, useSuper, fromInsideClass);
    }

    @Override
    public void setProperty(Object object, String property, Object newValue) {
        adaptee.setProperty(object, property, newValue);
    }
    
    @Override
    public void setProperty(Class sender, Object object, String name, Object newValue, boolean useSuper, boolean fromInsideClass) {
        adaptee.setProperty(sender, object, name, newValue, useSuper, fromInsideClass);
    }


    @Override
    public ClassNode getClassNode() {
        return adaptee.getClassNode();
    }

    @Override
    public List<MetaMethod> getMetaMethods() {
        return adaptee.getMetaMethods();
    }

    @Override
    public int selectConstructorAndTransformArguments(int numberOfConstructors, Object[] arguments) {
        return adaptee.selectConstructorAndTransformArguments(numberOfConstructors, arguments);
    }

    @Override
    public MetaMethod pickMethod(String methodName, Class[] arguments) {
        return adaptee.pickMethod(methodName, arguments);
    }

    @Override
    public Object getAttribute(Object object, String attribute) {
        return adaptee.getAttribute(object, attribute);
    }

    @Override
    public void setAttribute(Object object, String attribute, Object newValue) {
        adaptee.setAttribute(object, attribute, newValue);
    }
    
    @Override
    public List respondsTo(Object obj, String name, Object[] argTypes) {
        return adaptee.respondsTo(obj, name, argTypes);
    }

    @Override
    public List respondsTo(Object obj, String name) {
        return adaptee.respondsTo(obj, name);
    }

    @Override
    public MetaProperty hasProperty(Object obj, String name) {
        return adaptee.hasProperty(obj, name);
    }

    @Override
    public MetaMethod getStaticMetaMethod(String name, Object[] argTypes) {
        return adaptee.getStaticMetaMethod(name, argTypes);
    }

    @Override
    public MetaMethod getMetaMethod(String name, Object[] argTypes) {
        return adaptee.getMetaMethod(name, argTypes);
    }

    @Override
    public Class getTheClass() {
        return adaptee.getTheClass();
    }


// MetaCassImpl

    @Override
    public MetaClassRegistry getRegistry() {
        return metaClassImplDelegate().getRegistry();
    }

    @Override
    public boolean isGroovyObject() {
        return metaClassImplDelegate().isGroovyObject();
    }

    @Override
    public void addNewInstanceMethod(Method method) {
        metaClassImplDelegate().addNewInstanceMethod(method);
    }

    @Override
    public void addNewStaticMethod(Method method) {
        metaClassImplDelegate().addNewStaticMethod(method);
    }

    @Override
    public MetaMethod getMethodWithCaching(Class sender, String methodName, Object[] arguments, boolean isCallToSuper) {
        return metaClassImplDelegate().getMethodWithCaching(sender, methodName, arguments, isCallToSuper);
    }

    @Override
    public Constructor retrieveConstructor(Class[] arguments) {
        return metaClassImplDelegate().retrieveConstructor(arguments);
    }

    @Override
    public MetaMethod retrieveStaticMethod(String methodName, Object[] arguments) {
        return metaClassImplDelegate().retrieveStaticMethod(methodName, arguments);
    }

    @Override
    public MetaMethod getMethodWithoutCaching(Class sender, String methodName, Class[] arguments, boolean isCallToSuper) {
        return metaClassImplDelegate().getMethodWithoutCaching(sender, methodName, arguments, isCallToSuper);
    }

    @Override
    public void setProperties(Object bean, Map map) {
        metaClassImplDelegate().setProperties(bean, map);
    }

    @Override
    public MetaProperty getEffectiveGetMetaProperty(Class sender, Object object, String name, boolean useSuper) {
        return metaClassImplDelegate().getEffectiveGetMetaProperty(sender, object, name, useSuper);
    }

    @Override
    public void addMetaBeanProperty(MetaBeanProperty mp) {
        metaClassImplDelegate().addMetaBeanProperty(mp);
    }

    @Override
    public Object getAttribute(Class sender, Object object, String attribute, boolean useSuper, boolean fromInsideClass) {
        return metaClassImplDelegate().getAttribute(sender, object, attribute, useSuper, fromInsideClass);
    }

    @Override
    public void addMetaMethod(MetaMethod method) {
        metaClassImplDelegate().addMetaMethod(method);
    }

    @Override
    public CallSite createPogoCallCurrentSite(CallSite site, Class sender, Object[] args) {
        // do not delegate call site creation
        return super.createPogoCallCurrentSite(site, sender, args);
    }

    @Override
    public ClassInfo getClassInfo() {
        return metaClassImplDelegate().getClassInfo();
    }

    @Override
    public int getVersion() {
        return metaClassImplDelegate().getVersion();
    }

    @Override
    public void incVersion() {
        metaClassImplDelegate().incVersion();
    }

    @Override
    public MetaMethod[] getAdditionalMetaMethods() {
        return metaClassImplDelegate().getAdditionalMetaMethods();
    }
    

// ExpandoMetaClass

    @Override
    public void registerInstanceMethod(String name, Closure closure) {
        expandableDelegate().registerInstanceMethod(name, closure);
    }

    @Override
    public void registerSubclassInstanceMethod(String name, Class klazz, Closure closure) {
        expandableDelegate().registerSubclassInstanceMethod(name, klazz, closure);
    }

    @Override
    public void registerBeanProperty(String property, Object newValue) {
        expandableDelegate().registerBeanProperty(property, newValue);
    }
    
    @Override
    public Object invokeMethod(String name, Object args) {
        return expandableDelegate().invokeMethod(name, args);
    }

    @Override
    public Object getProperty(String property) {
        return expandableDelegate().getProperty(property);
    }

    @Override
    public String getPropertyForSetter(String setterName) {
        return expandableDelegate().getPropertyForSetter(setterName);
    }

    @Override
    public void setProperty(String property, Object newValue) {
        expandableDelegate().setProperty(property, newValue);
    }

    @Override
    public boolean isSetter(String name, CachedClass[] args) {
        return expandableDelegate().isSetter(name, args);
    }

    @Override
    public MetaMethod findMixinMethod(String methodName, Class[] arguments) {
        return expandableDelegate().findMixinMethod(methodName, arguments);
    }

    @Override
    public boolean isModified() {
        return expandableDelegate().isModified();
    }

    @Override
    public void registerSubclassInstanceMethod(MetaMethod metaMethod) {
        expandableDelegate().registerSubclassInstanceMethod(metaMethod);
    }

    @Override
    public void addMixinClass(MixinInMetaClass mixin) {
        expandableDelegate().addMixinClass(mixin);
    }

    @Override
    public Object castToMixedType(Object obj, Class type) {
        return expandableDelegate().castToMixedType(obj, type);
    }

    @Override
    public MetaClass getMetaClass() {
        return expandableDelegate().getMetaClass();
    }

    @Override
    public void setMetaClass(MetaClass metaClass) {
        expandableDelegate().setMetaClass(metaClass);
    }

    @Override
    public ExpandoMetaClass define(Closure closure) {
        return expandableDelegate().define(closure);
    }

    @Override
    public void registerInstanceMethod(MetaMethod metaMethod) {
        expandableDelegate().registerInstanceMethod(metaMethod);
    }

    @Override
    public List<MetaMethod> getMethods() {
        return expandableDelegate().getMethods();
    }
    
    @Override
    public List<MetaProperty> getProperties() {
        return expandableDelegate().getProperties();
    }

    @Override
    public Class getJavaClass() {
        return expandableDelegate().getJavaClass();
    }

    @Override
    public void refreshInheritedMethods(Set modifiedSuperExpandos) {
        expandableDelegate().refreshInheritedMethods(modifiedSuperExpandos);
    }

    @Override
    public List<MetaMethod> getExpandoMethods() {
        return expandableDelegate().getExpandoMethods();
    }

    @Override
    public Collection<MetaProperty> getExpandoProperties() {
        return expandableDelegate().getExpandoProperties();
    }

    @Override
    public MetaProperty getMetaProperty(String name) {
        return expandableDelegate().getMetaProperty(name);
    }

    @Override
    public boolean hasMetaProperty(String name) {
        return expandableDelegate().hasMetaProperty(name);
    }

    @Override
    public boolean hasMetaMethod(String name, Class[] args) {
        return expandableDelegate().hasMetaMethod(name, args);
    }

    @Override
    public CallSite createPojoCallSite(CallSite site, Object receiver, Object[] args) {
        // do not delegate call site creation
        return super.createPojoCallSite(site, receiver, args);
    }

    @Override
    public CallSite createStaticSite(CallSite site, Object[] args) {
        // do not delegate call site creation
        return super.createStaticSite(site, args);
    }

    @Override
    public CallSite createPogoCallSite(CallSite site, Object[] args) {
        // do not delegate call site creation
        return super.createPogoCallSite(site, args);
    }

    @Override
    public CallSite createPogoCallCurrentSite(CallSite site, Class sender, String name, Object[] args) {
        // do not delegate call site creation
        return super.createPogoCallCurrentSite(site, sender, name, args);
    }

    @Override
    public CallSite createConstructorSite(CallSite site, Object[] args) {
        // do not delegate call site creation
        return super.createConstructorSite(site, args);
    }

    /* $if version >= 2.1.0 $ */
    @Override
    public MetaMethod retrieveConstructor(Object[] args) {
        return expandableDelegate().retrieveConstructor(args);
    }
    /* $endif$ */

// AdaptingMetaClass

    @Override
    public MetaClass getAdaptee() {
        return adaptee;
    }

    @Override
    public void setAdaptee(MetaClass metaClass) {
        adaptee = metaClass;
    }
}
