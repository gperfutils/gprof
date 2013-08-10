package groovyx.gprof;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CallInterceptor {

    private ConcurrentMap<Thread, LocalInterceptor> interceptors;
    private MethodCallFilter methodFilter;
    private ThreadRunFilter threadFilter;
    private long interceptOverhead;
    
    public void setInterceptOverhead(long ns) {
       this.interceptOverhead = ns;
    }

    public CallInterceptor(MethodCallFilter methodFilter, ThreadRunFilter threadFilter) {
        interceptors = new ConcurrentHashMap<Thread, LocalInterceptor>();
        this.methodFilter = methodFilter;
        this.threadFilter = threadFilter;
    }
    
    public void clear() {
        interceptors.clear();
    }
    
    private LocalInterceptor getLocalInterceptor() {
        Thread thread = Thread.currentThread();
        LocalInterceptor theInterceptor = interceptors.get(thread);
        if (theInterceptor == null) {
            if (threadFilter.accept(thread)) {
                theInterceptor = new LocalInterceptor(methodFilter);
            } else {
                theInterceptor = LocalInterceptor.DO_NOT_INTERCEPT;
            }
            theInterceptor.setInterceptOverhead(interceptOverhead);
            interceptors.put(Thread.currentThread(), theInterceptor);
        }
        return theInterceptor;
    }
    
    public void beforeInvoke(MethodCallInfo methodCall) {
        getLocalInterceptor().beforeInvoke(methodCall);
    }

    public void afterInvoke(MethodCallInfo methodCall) {
        getLocalInterceptor().afterInvoke(methodCall);
    }

    public CallTree getTree() {
        return makeTree();
    }

    CallTree makeTree() {
        CallTree tree = new CallTree(Thread.currentThread());
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

    static class LocalInterceptor {

        private CallTree tree;
        private CallTree tmpTree;
        private Stack<CallTree.Node> nodeStack;
        private MethodCallFilter methodFilter;
        private long interceptOverhead;
        
        private static LocalInterceptor DO_NOT_INTERCEPT = new LocalInterceptor(null) {
            public void beforeInvoke(MethodCallInfo methodCall) { }
            public void afterInvoke(MethodCallInfo methodCall) { }
        };

        public LocalInterceptor(MethodCallFilter methodFilter) {
            tmpTree = new CallTree(Thread.currentThread());
            nodeStack = new Stack();
            nodeStack.push(tmpTree.getRoot());
            this.methodFilter = methodFilter;
        }

        public void setInterceptOverhead(long ns) {
            this.interceptOverhead = ns;
        }

        public void beforeInvoke(MethodCallInfo methodCall) {
            CallTree.Node node = new CallTree.Node(methodCall);
            CallTree.Node parentNode = nodeStack.peek();
            node.setParent(parentNode);
            parentNode.addChild(node);
            nodeStack.push(node);
        }
        
        public void afterInvoke(MethodCallInfo methodCall) {
            nodeStack.pop();
        }

        public CallTree getTree() {
            if (tree == null) {
                tree = makeTree();
            }
            return tree;
        }
        
        private void setOverhead(CallTree.Node node) {
            CallInfo call = node.getData();
            for (CallTree.Node child : node.getChildren()) {
                setOverhead(child);
                call.setOverheadTime(call.getOverheadTime() + child.getData().getOverheadTime());
            }
        }
        
        CallTree makeTree() {
            final CallTree tree = tmpTree;
            tree.visit(new CallTree.NodeVisitor() {
                @Override
                public void visit(CallTree.Node node) {
                    setOverhead(node);
                }
            });
            tree.visit(new CallTree.NodeVisitor() {
                @Override
                public void visit(CallTree.Node node) {
                    CallInfo call = node.getData();
                    CallTree.Node parentNode = node.getParent();
                    if (node != tree.getRoot()) {
                        CallInfo parentCall = parentNode.getData();
                        parentCall.setChildrenTime(parentCall.getChildrenTime() + call.getTime());
                        parentCall.setTime(parentCall.getTime() - call.getOverheadTime());
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