package gprof;

import groovy.lang.Interceptor;

import java.util.Stack;

public class ProfileInterceptor implements Interceptor {

    private ProfileCallTree tree;
    private Stack<ProfileCallTree.Node> nodeStack;
    private Stack<Long> timeStack;

    public ProfileInterceptor() {
        tree = new ProfileCallTree();
        nodeStack = new Stack();
        nodeStack.push(tree.getRoot());
        timeStack = new Stack();
    }


    @Override
    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        String className;
        if (object.getClass() == Class.class /* static methods */) {
            className = ((Class) object).getName();
        } else {
            className = object.getClass().getName();
        }
        ProfileCallTree.Node node = new ProfileCallTree.Node(new ProfileCallEntry(className, methodName));
        ProfileCallTree.Node parentNode = nodeStack.peek();
        parentNode.addChild(node);
        node.setParent(parentNode);

        nodeStack.push(node);
        timeStack.push(System.nanoTime());

        return null;
    }

    @Override
    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        long time = System.nanoTime() - timeStack.pop();
        ProfileCallTree.Node node = nodeStack.pop();
        if (node.hasChildren()) {
            for (ProfileCallTree.Node child : node.getChildren()) {
                time -= child.getData().getTime().nanoseconds();
            }
        }
        node.getData().setTime(new ProfileTime(time));
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