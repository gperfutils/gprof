package gprof;

import groovy.lang.*;

import java.beans.IntrospectionException;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProfileInterceptor implements Interceptor {

    private ConcurrentMap<Thread, LocalInterceptor> interceptors;
    private ProfileCallTree tree;
    private ProfileMethodFilter methodFilter;
    private ProfileThreadFilter threadFilter;

    public ProfileInterceptor(ProfileMethodFilter methodFilter, ProfileThreadFilter threadFilter) {
        interceptors = new ConcurrentHashMap<Thread, LocalInterceptor>();
        this.methodFilter = methodFilter;
        this.threadFilter = threadFilter;
    }

    private LocalInterceptor getLocalInterceptor() {
        Thread thread = Thread.currentThread();
        if (!threadFilter.accept(thread)) {
            return LocalInterceptor.DO_NOT_INTERCEPT;
        }
        LocalInterceptor theInterceptor = interceptors.get(Thread.currentThread());
        if (theInterceptor == null) {
            theInterceptor = new LocalInterceptor(methodFilter, threadFilter);
            interceptors.put(Thread.currentThread(), theInterceptor);
        }
        return theInterceptor;
    }

    @Override
    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        return getLocalInterceptor().beforeInvoke(object, methodName, arguments);
    }

    @Override
    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        return getLocalInterceptor().afterInvoke(object, methodName, arguments, result);
    }

    @Override
    public boolean doInvoke() {
        return getLocalInterceptor().doInvoke();
    }

    public ProfileCallTree getTree() {
        return makeTree();
    }

    ProfileCallTree makeTree() {
        // Wait for all the child threads to die.
        Thread profThread = Thread.currentThread();
        for (Thread thread : interceptors.keySet()) {
            if (!thread.equals(profThread)) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ProfileCallTree tree = new ProfileCallTree(profThread);
        for (LocalInterceptor interceptor : interceptors.values()) {
            ProfileCallTree theTree = interceptor.getTree();
            ProfileThreadEntry theThread = (ProfileThreadEntry) theTree.getRoot().getData();
            if (theThread.equals(profThread)) {
                for (ProfileCallTree.Node child : theTree.getRoot().getChildren()) {
                    tree.getRoot().addChild(child);
                }
            } else {
                tree.getRoot().addChild(theTree.getRoot());
            }
        }
        return tree;
    }

    static class LocalInterceptor implements Interceptor {

        private ProfileCallTree tree;
        private ProfileCallTree tmpTree;
        private Stack<ProfileCallTree.Node> nodeStack;
        private Stack<Long> timeStack;
        private boolean ignoring;
        private ProfileMethodFilter methodFilter;
        private ProfileThreadFilter threadFilter;

        private static LocalInterceptor DO_NOT_INTERCEPT = new LocalInterceptor(null, null) {
            @Override
            public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
                return null;
            }
            @Override
            public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
                return result;
            }
        };

        public LocalInterceptor(ProfileMethodFilter methodFilter, ProfileThreadFilter threadFilter) {
            tmpTree = new ProfileCallTree(Thread.currentThread());
            nodeStack = new Stack();
            nodeStack.push(tmpTree.getRoot());
            timeStack = new Stack();
            this.methodFilter = methodFilter;
            this.threadFilter = threadFilter;
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
            ProfileCallTree.Node node = new ProfileCallTree.Node(new ProfileCallEntry(classNameOf(object), methodName));
            ProfileCallTree.Node parentNode = nodeStack.peek();
            node.setParent(parentNode);
            parentNode.addChild(node);

            nodeStack.push(node);
            timeStack.push(System.nanoTime());
            return null;
        }

        @Override
        public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
            long time = System.nanoTime() - timeStack.pop();
            ProfileCallTree.Node node = nodeStack.pop();
            node.getData().setTime(new ProfileTime(time));
            return result;
        }

        @Override
        public boolean doInvoke() {
            return true;
        }

        public ProfileCallTree getTree() {
            if (tree == null) {
                tree = makeTree();
            }
            return tree;
        }

        ProfileCallTree makeTree() {
            final ProfileCallTree tree = tmpTree;
            tree.visit(new ProfileCallTree.NodeVisitor() {
                @Override
                public void visit(ProfileCallTree.Node node) {
                    ProfileEntry entry = node.getData();
                    ProfileCallTree.Node parentNode = node.getParent();
                    if (node != tree.getRoot()) {
                        ProfileEntry parentCall = parentNode.getData();
                        parentCall.setTime(parentCall.getTime().minus(entry.getTime()));
                    }
                }
            });
            tree.visit(new ProfileCallTree.NodeVisitor() {
                @Override
                public void visit(ProfileCallTree.Node node) { }
                @Override
                public void exit(ProfileCallTree.Node node) {
                    ProfileEntry entry = node.getData();
                    if (entry instanceof ProfileCallEntry) {
                        ProfileCallEntry call = (ProfileCallEntry) entry;
                        if (!methodFilter.accept(call.getClassName(), call.getMethodName())) {
                            ProfileCallTree.Node parentNode = node.getParent();
                            parentNode.removeChild(node);
                            for (ProfileCallTree.Node child : node.getChildren()) {
                                parentNode.addChild(child);
                            }
                        }
                    }
                }
            });
            return tree;
        }

    }

}