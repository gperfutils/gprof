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
import java.util.List;

public class ProfileCallTree {

    private Node root;

    public ProfileCallTree() {
        this.root = new Node(null);
    }

    public Node getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public static class Node {

        private ProfileCallEntry data;
        private Node parent;
        private List<Node> children;

        public Node(ProfileCallEntry data) {
            this.data = data;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Node getParent() {
            return parent;
        }

        public void addChild(Node child) {
            getChildren().add(child);
        }

        public List<Node> getChildren() {
            if (children == null) {
                children = new ArrayList(1);
            }
            return children;
        }

        public ProfileCallEntry getData() {
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

        public void walk(Closure c) {
            for (Node child : getChildren()) {
                c.call(child);
                child.walk(c);
            }
        }
    }

}
