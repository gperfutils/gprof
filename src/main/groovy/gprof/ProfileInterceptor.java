package gprof;

import groovy.lang.Interceptor;

import java.util.List;
import java.util.Stack;

public class ProfileInterceptor implements Interceptor {

    private ProfileCallTree tree;
    private Stack<ProfileCallTree.Node> nodeStack;
    private Stack<Long> timeStack;
    private boolean ignoring;

    public ProfileInterceptor() {
        tree = new ProfileCallTree();
        nodeStack = new Stack();
        nodeStack.push(tree.getRoot());
        timeStack = new Stack();
    }

    private boolean isCallToBeExcluded(Object object, String methodName) {
        return isCallToBeExcluded(classNameOf(object), methodName);
    }

    private boolean isCallToBeExcluded(String className, String methodName) {
        return className.equals("groovy.grape.Grape") && methodName.equals("grab");
    }

    private String classNameOf(Object object) {
        String className;
        if (object.getClass() == Class.class /* static methods */) {
            className = ((Class) object).getName();
        } else {
            className = object.getClass().getName();
        }
        return className;
    }

    @Override
    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        if (ignoring) {
            // Skip while processing a call to be excluded.
            return null;
        }
        ignoring = isCallToBeExcluded(object, methodName);

        ProfileCallTree.Node node = new ProfileCallTree.Node(new ProfileCallEntry(classNameOf(object), methodName));
        ProfileCallTree.Node parentNode = nodeStack.peek();
        parentNode.addChild(node);
        node.setParent(parentNode);

        nodeStack.push(node);
        timeStack.push(System.nanoTime());
        return null;
    }

    @Override
    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        if (ignoring && (ignoring = !isCallToBeExcluded(object, methodName))) {
            // Skip while processing a call to be excluded.
            return result;
        }
        long time = System.nanoTime() - timeStack.pop();
        ProfileCallTree.Node node = nodeStack.pop();
        if (node.hasChildren()) {
            for (ProfileCallTree.Node child : node.getChildren()) {
                ProfileCallEntry callEntry = child.getData();
                time -= callEntry.getTime().nanoseconds();
            }
        }
        node.getData().setTime(new ProfileTime(time));
        List<ProfileCallTree.Node> childNodes = node.getChildren();
        for (int i = childNodes.size() - 1; i >= 0; i--) {
            ProfileCallTree.Node child = childNodes.get(i);
            ProfileCallEntry childCall = child.getData();
            if (isCallToBeExcluded(childCall.getClassName(), childCall.getMethodName())) {
                childNodes.remove(i);
            }
        }
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