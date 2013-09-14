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

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CallInterceptor {

    private ConcurrentMap<Thread, LocalInterceptor> interceptors;
    private MethodCallFilter methodFilter;
    private ThreadRunFilter threadFilter;

    public CallInterceptor(MethodCallFilter methodFilter, ThreadRunFilter threadFilter) {
        interceptors = new ConcurrentHashMap<Thread, LocalInterceptor>();
        this.methodFilter = methodFilter;
        this.threadFilter = threadFilter;
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

    private CallTree makeTree() {
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
        
        private CallTree makeTree() {
            CallTree tree = tmpTree;
            sumUpOverheadTime(tree);
            subtractOverheadTime(tree);
            setChildrenTime(tree);
            filterMethods(tree);
            return tree;
        }

        private void filterMethods(CallTree tree) {
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
        }
        
        private void sumUpOverheadTime(CallTree tree) {
            tree.visit(new CallTree.NodeVisitor() {
                @Override
                public void visit(CallTree.Node node) {
                }
                @Override
                public void exit(CallTree.Node node) {
                    if (node.hasParent()) {
                        CallInfo call = node.getData();
                        CallInfo parentCall = node.getParent().getData();
                        parentCall.setOverheadTime(parentCall.getOverheadTime() + call.getOverheadTime());
                    }
                }
            });
        }
        
        private void subtractOverheadTime(CallTree tree) {
            tree.visit(new CallTree.NodeVisitor() {
                @Override
                public void visit(CallTree.Node node) {
                    CallInfo call = node.getData();
                    if (node.hasParent()) {
                        CallInfo parentCall = node.getParent().getData();
                        parentCall.setTime(parentCall.getTime() - call.getOverheadTime());
                    }
                }
            });
        }

        private void setChildrenTime(CallTree tree) {
            tree.visit(new CallTree.NodeVisitor() {
                @Override
                public void visit(CallTree.Node node) {
                    if (node.hasParent()) {
                        CallInfo call = node.getData();
                        CallInfo parentCall = node.getParent().getData();
                        if (parentCall instanceof ThreadRunInfo) {
                            parentCall.setTime(parentCall.getTime() + call.getTime());
                        }
                        parentCall.setChildrenTime(parentCall.getChildrenTime() + call.getTime());
                    }
                }
            });
        }

    }

}