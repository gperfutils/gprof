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
import java.util.*;

public class Profile {

    private List<ProfileMethodEntry> methodEntries;

    public Profile(ProfileCallTree tree) {
        methodEntries = makeMethodEntries(tree);
    }

    private List<ProfileMethodEntry> makeMethodEntries(ProfileCallTree tree) {
        final List<ProfileMethodEntry> entries = new ArrayList();
        final ProfileTime[] time = { new ProfileTime(0) };

        tree.visit(new ProfileCallTree.NodeVisitor() {

            Map<String, ProfileMethodEntry> methodEntryMap = new HashMap();

            public void visit(ProfileCallTree.Node node) {
                ProfileCallEntry callEntry = node.getData();
                String key = String.format("%s.%s", callEntry.getClassName(), callEntry.getMethodName());
                ProfileMethodEntry methodEntry = methodEntryMap.get(key);
                if (methodEntry == null) {
                    methodEntry = new ProfileMethodEntry(callEntry.getClassName(), callEntry.getMethodName());
                    methodEntryMap.put(key, methodEntry);
                    entries.add(methodEntry);
                }
                methodEntry.getCallEntries().add(callEntry);
                ProfileTime theTime = callEntry.getTime();
                if (methodEntry.getTime() == null) {
                    methodEntry.setTime(theTime);
                } else {
                    methodEntry.setTime(methodEntry.getTime().plus(theTime));
                }
                if (methodEntry.getMinTime() == null || methodEntry.getMinTime().compareTo(theTime) > 0) {
                    methodEntry.setMinTime(theTime);
                }
                if (methodEntry.getMaxTime() == null || methodEntry.getMaxTime().compareTo(theTime) < 0) {
                    methodEntry.setMaxTime(theTime);
                }
                time[0] = time[0].plus(theTime);
            }
        });
        for (ProfileMethodEntry methodEntry : entries) {
            methodEntry.setPercent(methodEntry.getTime().milliseconds() / time[0].milliseconds() * 100);
            methodEntry.setTimePerCall(methodEntry.getTime().div(methodEntry.getCallEntries().size()));
        }
        return entries;
    }

    public void prettyPrint() {
        prettyPrint(new PrintWriter(System.out));
    }

    public void prettyPrint(PrintWriter writer) {
        prettyPrint(writer, new DefaultComparator());
    }

    public void prettyPrint(PrintWriter writer, Comparator<ProfileMethodEntry> comparator) {
        new ProfileFlatPrinter(methodEntries).print(writer, comparator);
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
