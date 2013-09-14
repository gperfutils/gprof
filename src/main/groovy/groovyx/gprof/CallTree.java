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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallTree {

    private Node root;

    public CallTree(Thread thread) {
        this.root = new Node(new ThreadRunInfo(thread));
    }

    public void visit(NodeVisitor visitor) {
        visitor.visit(root);
        root.visit(visitor);
        visitor.exit(root);
    }

    public Node getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public static class Node {

        private CallInfo data;
        private Node parent;
        private List<Node> children = new ArrayList();

        public Node(CallInfo data) {
            this.data = data;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Node getParent() {
            return parent;
        }

        public boolean hasParent() {
            return parent != null;
        }

        public void addChild(Node child) {
            children.add(child);
            child.setParent(this);
        }

        public void removeChild(Node child) {
            children.remove(child);
            child.setParent(null);
        }

        public List<Node> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public boolean hasChildren() {
            return children != null;
        }

        public CallInfo getData() {
            return data;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(String.format("%s", data));
            for (Node child : getChildren()) {
                sb.append("\n");
                sb.append(String.format("    %s", child));
            }
            return sb.toString();
        }

        public void visit(NodeVisitor visitor) {
            for (Node child : new ArrayList<Node>(getChildren())) {
                visitor.visit(child);
                child.visit(visitor);
                visitor.exit(child);
            }
        }
    }

    public static abstract class NodeVisitor {

        public abstract void visit(Node node);
        public void exit(Node node) {}

    }

}
