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
package gprof;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ProfileGraphNormalizer {

    public List<ProfileGraphEntry> normalize(ProfileCallTree callTree) {
        final Map<Long, ProfileGraphEntry> graphTable = new HashMap();

        callTree.visit(new ProfileCallTree.NodeVisitor() {
            Stack<Map<String, Long>> indexTableStack = new Stack();
            long lastIndex = 0;
            ProfileThreadEntry thread;
            Stack<Long> indexStack = new Stack();
            @Override
            public void visit(ProfileCallTree.Node node) {
                ProfileEntry entry = node.getData();
                if (entry instanceof ProfileThreadEntry) {
                    thread = (ProfileThreadEntry) entry;
                    Map<String, Long> indexTable = new HashMap();
                    for (ProfileCallTree.Node child : node.getChildren()) {
                        if (child.getData() instanceof ProfileCallEntry) {
                            ProfileCallEntry childCall = (ProfileCallEntry) child.getData();
                            long index;
                            if (indexTable.containsKey(childCall.getName())) {
                               index = indexTable.get(childCall.getName());
                            } else {
                                index = ++lastIndex;
                                indexTable.put(childCall.getName(), index);
                            }
                        }
                    }
                    indexTableStack.push(indexTable);
                } else if (entry instanceof ProfileCallEntry) {
                    ProfileCallEntry call = (ProfileCallEntry) entry;
                    long index = indexTableStack.peek().get(call.getName());

                    ProfileGraphEntry graph = graphTable.get(index);
                    if (graph == null) {
                        ProfileMethodEntry method = new ProfileMethodEntry(call.getClassName(), call.getMethodName());
                        method.setTime(call.getTime());
                        graph = new ProfileGraphEntry(index, thread, method, indexTableStack.size() - 1);
                        graph.setTime(call.getTime());
                        graphTable.put(index, graph);
                    }
                    ProfileMethodEntry method = graph.getMethod();
                    method.getCallEntries().add(call);

                    Map<String, Long> indexTable = new HashMap();
                    List childNames = new ArrayList();
                    for (ProfileGraphEntry.Child child : graph.getChildren()) {
                        indexTable.put(graphTable.get(child.getIndex()).getMethod().getName(), child.getIndex());
                    }
                    for (ProfileCallTree.Node child : node.getChildren()) {
                        ProfileCallEntry childCall = (ProfileCallEntry) child.getData();
                        if (!indexTable.containsKey(childCall.getName())) {
                            long childIndex = ++lastIndex;
                            float childTimePercent = childCall.getTime().nanoseconds() / call.getTime().nanoseconds();
                            graph.addChild(new ProfileGraphEntry.Child(childIndex));
                            graph.setChildrenTime(graph.getChildrenTime().plus(childCall.getTime()));
                            indexTable.put(childCall.getName(), childIndex);
                        }
                    }
                    indexTableStack.push(indexTable);
                }
            }
            @Override
            public void exit(ProfileCallTree.Node node) {
                if (!indexTableStack.isEmpty()) {
                    indexTableStack.pop();
                }
                if (node.getData() instanceof ProfileThreadEntry) {
                    ProfileThreadEntry thread = (ProfileThreadEntry) node.getData();
                    long totalTime = 0;
                    for (ProfileGraphEntry graph : graphTable.values()) {
                        if (graph.getThread() == thread) {
                            totalTime += graph.getSelfTime().nanoseconds();
                        }
                    }
                    thread.setTime(new ProfileTime(totalTime));
                    for (ProfileGraphEntry graph : graphTable.values()) {
                        if (graph.getThread() == thread) {
                            graph.setTimePercent(((float) graph.getTime().nanoseconds()) / thread.getTime().nanoseconds() * 100f);
                        }
                    }
                } else if (node.getData() instanceof ProfileCallEntry) {
                    ProfileCallEntry call = (ProfileCallEntry) node.getData();
                    long index = indexTableStack.peek().get(call.getName());
                    ProfileGraphEntry graph = graphTable.get(index);
                    graph.setSelfTime(graph.getTime().minus(graph.getChildrenTime()));
                    for (ProfileGraphEntry.Child graphChild : graph.getChildren()) {
                        ProfileGraphEntry childRef = graphTable.get(graphChild.getIndex());
                        graphChild.setTimePercent((float) (childRef.getTime().nanoseconds()) / graph.getTime().nanoseconds() * 100f);
                    }
                }
            }
        });
        return new ArrayList(graphTable.values());
    }

}
