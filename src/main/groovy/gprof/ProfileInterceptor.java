package gprof;

import groovy.lang.Interceptor;

public class ProfileInterceptor implements Interceptor {

    private ProfileCallTree tree;
    private ProfileCallTree.Node currentNode;

    public ProfileInterceptor() {
        tree = new ProfileCallTree();
        currentNode = tree.getRoot();
    }

    @Override
    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        String className;
        if (object.getClass() == Class.class && methodName.equals("ctor")) {
            className = ((Class) object).getName();
        } else {
            className = object.getClass().getName();
        }
        ProfileCallEntry callEntry = new ProfileCallEntry(className, methodName);
        callEntry.setStartTime(new ProfileTime(System.nanoTime()));
        ProfileCallTree.Node theCall = new ProfileCallTree.Node(callEntry);
        currentNode.addChild(theCall);
        theCall.setParent(currentNode);
        currentNode = theCall;
        return null;
    }

    @Override
    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        currentNode.getData().setEndTime(new ProfileTime(System.nanoTime()));
        currentNode = currentNode.getParent();
        return result;
    }

    @Override
    public boolean doInvoke() {
        return true;
    }

    public ProfileCallTree getTree() {
        return tree;
    }
}