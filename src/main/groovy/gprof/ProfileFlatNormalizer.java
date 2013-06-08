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

public class ProfileFlatNormalizer {

    public List<ProfileMethodEntry> normalize(ProfileCallTree callTree) {
        return makeMethodEntries(callTree);
    }

    List<ProfileMethodEntry> makeMethodEntries(ProfileCallTree tree) {
        final List<ProfileMethodEntry> entries = new ArrayList();
        final ProfileTime[] time = { new ProfileTime(0) };

        tree.visit(new ProfileCallTree.NodeVisitor() {

            Map<String, ProfileMethodEntry> methodEntryMap = new HashMap();

            public void visit(ProfileCallTree.Node node) {
                ProfileEntry entry = node.getData();
                if (entry instanceof ProfileCallEntry) {
                    ProfileCallEntry callEntry = (ProfileCallEntry) entry;
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
            }
        });
        for (ProfileMethodEntry methodEntry : entries) {
            methodEntry.setPercent(methodEntry.getTime().milliseconds() / time[0].milliseconds() * 100);
            methodEntry.setTimePerCall(methodEntry.getTime().div(methodEntry.getCallEntries().size()));
        }
        return entries;
    }

}
