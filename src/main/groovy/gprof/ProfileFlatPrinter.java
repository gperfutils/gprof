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

public class ProfileFlatPrinter {

    private static String SP = "  ";

    private ProfileTime time = new ProfileTime(0L);
    private List<ProfileMethodEntry> methodEntries = new ArrayList();

    public ProfileFlatPrinter(ProfileCallTree tree) {
        final Map<String, ProfileMethodEntry> methodEntryMap = new HashMap();
        tree.visit(new ProfileCallTree.NodeVisitor() {
            public void visit(ProfileCallTree.Node node) {
                ProfileCallEntry callEntry = node.getData();
                String key = String.format("%s.%s", callEntry.getClassName(), callEntry.getMethodName());
                ProfileMethodEntry methodEntry = methodEntryMap.get(key);
                if (methodEntry == null) {
                    methodEntry = new ProfileMethodEntry(callEntry.getClassName(), callEntry.getMethodName());
                    methodEntryMap.put(key, methodEntry);
                    methodEntries.add(methodEntry);
                }
                methodEntry.getCallEntries().add(callEntry);
                ProfileTime theTime = callEntry.getEndTime().minus(callEntry.getStartTime());
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
                if (node.hasParent()) {
                    ProfileCallEntry parent = node.getParent().getData();
                    if (parent != null) {
                        String parentKey = String.format("%s.%s", parent.getClassName(), parent.getMethodName());
                        ProfileMethodEntry parentMethodEntry = methodEntryMap.get(parentKey);
                        parentMethodEntry.setTime(parentMethodEntry.getTime().minus(theTime));
                    }
                }
                time = time.plus(theTime);
            }
        });
        for (ProfileMethodEntry methodEntry : methodEntries) {
            methodEntry.setTimePerCall(methodEntry.getTime().div(methodEntry.getCallEntries().size()));
        }
    }

    public void print(PrintWriter writer, Comparator<ProfileMethodEntry> comparator) {
        Collections.sort(methodEntries, comparator);

        List<Map<COLUMN, String>> rows = new ArrayList(methodEntries.size());
        for (ProfileMethodEntry methodEntry : methodEntries) {
            Map<COLUMN, String> row = new HashMap();
            row.put(COLUMN.TIME_PERCENT, String.format("%.2f", methodEntry.getTime().milliseconds() / time.milliseconds() * 100));
            row.put(COLUMN.TIME_TOTAL, String.format("%.2f", methodEntry.getTime().milliseconds()));
            row.put(COLUMN.CALLS, String.format("%d", methodEntry.getCallEntries().size()));
            row.put(COLUMN.TIME_MIN, String.format("%.2f", methodEntry.getMinTime().milliseconds()));
            row.put(COLUMN.TIME_MAX, String.format("%.2f", methodEntry.getMaxTime().milliseconds()));
            row.put(COLUMN.TIME_AVG, String.format("%.2f", methodEntry.getTimePerCall().milliseconds()));
            row.put(COLUMN.METHOD_NAME, methodEntry.getMethodName());
            row.put(COLUMN.CLASS_NAME, methodEntry.getClassName());
            rows.add(row);
        }

        Map<COLUMN, Integer> colSizeMap = new HashMap();
        for (COLUMN col : COLUMN.values()) {
            colSizeMap.put(col, col.name.length());
        }
        for (Map<COLUMN, String> row : rows) {
            for (COLUMN col : COLUMN.values()) {
                colSizeMap.put(col, Math.max(colSizeMap.get(col), row.get(col).length()));
            }
        }

        String headerFormat =  String.format(
                "%%-%ds" + // time percent
                "%s" + "%%-%ds" + // time total
                "%s" + "%%-%ds" + // calls
                "%s" + "%%-%ds" + // time min
                "%s" + "%%-%ds" + // time max
                "%s" + "%%-%ds" + // time avg
                "%s" + "%%-%ds" + // method name
                "%s" + "%%-%ds" + // class name
                "%n",
                colSizeMap.get(COLUMN.TIME_PERCENT), SP,
                colSizeMap.get(COLUMN.TIME_TOTAL), SP,
                colSizeMap.get(COLUMN.CALLS), SP,
                colSizeMap.get(COLUMN.TIME_MIN), SP,
                colSizeMap.get(COLUMN.TIME_MAX), SP,
                colSizeMap.get(COLUMN.TIME_AVG), SP,
                colSizeMap.get(COLUMN.METHOD_NAME), SP,
                colSizeMap.get(COLUMN.CLASS_NAME));
        writer.printf(headerFormat,
                COLUMN.TIME_PERCENT.name, COLUMN.TIME_TOTAL.name, COLUMN.CALLS.name,
                COLUMN.TIME_MIN.name, COLUMN.TIME_MAX.name, COLUMN.TIME_AVG.name,
                COLUMN.METHOD_NAME.name, COLUMN.CLASS_NAME.name);

        String rowFormat = String.format(
                "%%%ds" + // time percent
                "%s" + "%%%ds" + // time total
                "%s" + "%%%ds" + // calls
                "%s" + "%%%ds" + // time max
                "%s" + "%%%ds" + // time min
                "%s" + "%%%ds" + // time avg
                "%s" + "%%-%ds" + // method name
                "%s" + "%%-%ds" + // class name
                "%n",
                colSizeMap.get(COLUMN.TIME_PERCENT), SP,
                colSizeMap.get(COLUMN.TIME_TOTAL), SP,
                colSizeMap.get(COLUMN.CALLS), SP,
                colSizeMap.get(COLUMN.TIME_MIN), SP,
                colSizeMap.get(COLUMN.TIME_MAX), SP,
                colSizeMap.get(COLUMN.TIME_AVG), SP,
                colSizeMap.get(COLUMN.METHOD_NAME), SP,
                colSizeMap.get(COLUMN.CLASS_NAME));
        for (Map<COLUMN, String> row : rows) {
                writer.printf(rowFormat,
                        row.get(COLUMN.TIME_PERCENT), row.get(COLUMN.TIME_TOTAL), row.get(COLUMN.CALLS),
                        row.get(COLUMN.TIME_MIN), row.get(COLUMN.TIME_MAX), row.get(COLUMN.TIME_AVG),
                        row.get(COLUMN.METHOD_NAME), row.get(COLUMN.CLASS_NAME));
        }

        writer.flush();
    }

    public enum COLUMN {

        TIME_PERCENT("%"), TIME_TOTAL("sum(ms)"), CALLS("calls"),
        TIME_MAX("max(ms)"), TIME_MIN("min(ms)"), TIME_AVG("avg(ms)"),
        METHOD_NAME("method"), CLASS_NAME("class");

        private String name;

        COLUMN(String name) {
            this.name = name;
        }
    }

}
