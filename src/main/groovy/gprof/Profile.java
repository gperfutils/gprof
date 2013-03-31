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

import java.io.PrintWriter;
import java.util.Comparator;

public class Profile {

    private ProfileCallTree tree;

    public Profile(ProfileCallTree tree) {
        this.tree = tree;
    }

    public void prettyPrint() {
        prettyPrint(new PrintWriter(System.out));
    }

    public void prettyPrint(PrintWriter writer) {
        prettyPrint(writer, new DefaultComparator());
    }

    public void prettyPrint(PrintWriter writer, Comparator<ProfileMethodEntry> comparator) {
        new ProfileFlatPrinter(tree).print(writer, comparator);
    }

    private static class DefaultComparator implements Comparator<ProfileMethodEntry> {

        @Override
        public int compare(ProfileMethodEntry o1, ProfileMethodEntry o2) {
            int r = -(o1.getTime().compareTo(o2.getTime()));
            if (r == 0) {
                r = -(((Integer) o1.getCallEntries().size()).compareTo(o2.getCallEntries().size()));
                if (r == 0) {
                    r = o1.getClassName().compareTo(o2.getClassName());
                    if (r == 0) {
                        r = o1.getMethodName().compareTo(o2.getMethodName());
                    }
                }
            }
            return r;
        }
    }

}
