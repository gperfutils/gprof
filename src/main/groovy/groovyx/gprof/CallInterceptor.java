package groovyx.gprof;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CallInterceptor implements groovy.lang.Interceptor {

    private ConcurrentMap<Thread, LocalInterceptor> interceptors;
    private CallTree tree;
    private MethodCallFilter methodFilter;
    private ThreadRunFilter threadFilter;

    public CallInterceptor(MethodCallFilter methodFilter, ThreadRunFilter threadFilter) {
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

    public CallTree getTree() {
        return makeTree();
    }

    CallTree makeTree() {
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
        CallTree tree = new CallTree(profThread);
        ThreadRunInfo mainThreadRun = (ThreadRunInfo) tree.getRoot().getData();
        for (LocalInterceptor interceptor : interceptors.values()) {
            CallTree theTree = interceptor.getTree();
            ThreadRunInfo threadRun = (ThreadRunInfo) theTree.getRoot().getData();
            if (threadRun.equals(mainThreadRun)) {
                for (CallTree.Node child : theTree.getRoot().getChildren()) {
                    tree.getRoot().addChild(child);
                }
            } else {
                tree.getRoot().addChild(theTree.getRoot());
            }
        }
        return tree;
    }

    static class LocalInterceptor implements groovy.lang.Interceptor {

        private CallTree tree;
        private CallTree tmpTree;
        private Stack<CallTree.Node> nodeStack;
        private Stack<Long> timeStack;
        private boolean ignoring;
        private MethodCallFilter methodFilter;
        private ThreadRunFilter threadFilter;

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

        public LocalInterceptor(MethodCallFilter methodFilter, ThreadRunFilter threadFilter) {
            tmpTree = new CallTree(Thread.currentThread());
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
            CallTree.Node node = new CallTree.Node(new MethodCallInfo(classNameOf(object), methodName));
            CallTree.Node parentNode = nodeStack.peek();
            node.setParent(parentNode);
            parentNode.addChild(node);

            nodeStack.push(node);
            timeStack.push(System.nanoTime());
            return null;
        }

        @Override
        public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
            long time = System.nanoTime() - timeStack.pop();
            CallTree.Node node = nodeStack.pop();
            node.getData().setTime(time);
            return result;
        }

        @Override
        public boolean doInvoke() {
            return true;
        }

        public CallTree getTree() {
            if (tree == null) {
                tree = makeTree();
            }
            return tree;
        }

        CallTree makeTree() {
            final CallTree tree = tmpTree;
            tree.visit(new CallTree.NodeVisitor() {
                @Override
                public void visit(CallTree.Node node) {
                    CallInfo call = node.getData();
                    CallTree.Node parentNode = node.getParent();
                    if (node != tree.getRoot()) {
                        CallInfo parentCall = parentNode.getData();
                        parentCall.setChildrenTime(parentCall.getChildrenTime() + call.getTime());
                    }
                }
            });
            tree.visit(new CallTree.NodeVisitor() {
                @Override
                public void visit(CallTree.Node node) { }
                @Override
                public void exit(CallTree.Node node) {
                    CallInfo call = node.getData();
                    if (call instanceof MethodCallInfo) {
                        MethodCallInfo methodCall = (MethodCallInfo) call;
                        if (!methodFilter.accept(methodCall.getMethod())) {
                            CallTree.Node parentNode = node.getParent();
                            parentNode.removeChild(node);
                            for (CallTree.Node child : node.getChildren()) {
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