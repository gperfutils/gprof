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

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileTree<T> {

    public static class Node<T> {
        private T data;
        private Node<T> parent;
        private List<Node<T>> children;

        public Node(T data) {
            this.data = data;
            children = new ArrayList();
        }

        public void setParent(Node<T> parent) {
            this.parent = parent;
        }
        public Node<T> getParent() {
            return parent;
        }
        public void addChild(Node<T> child) {
            children.add(child);
        }

        public List<Node<T>> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public T getData() {
            return data;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(String.format("%s", data));
            for (Node<T> child : children) {
                sb.append("\n");
                sb.append(String.format("    %s", child));
            }
            return sb.toString();
        }

        public void walk(Closure c) {
            for (Node child: getChildren()) {
                c.call(child);
                child.walk(c);
            }
        }
    }

    private Node<T> root;

    public ProfileTree(Node<T> root) {
        this.root = root;
    }

    public Node<T> getRoot() {
        return root;
    }

    public void walk(Closure c) {
        doWalk(root, c);
    }

    private void doWalk(Node<T> node, Closure c) {
        c.call(node);
        for (Node child: node.getChildren()) {
            doWalk(child, c);
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
