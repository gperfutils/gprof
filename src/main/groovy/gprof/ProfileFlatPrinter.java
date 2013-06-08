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

public class ProfileFlatPrinter implements ProfilePrinter {

    private static String SP = "  ";

    private List<ProfileMethodEntry> methodEntries;

    public ProfileFlatPrinter(ProfileCallTree callTree) {
        this.methodEntries = new ProfileFlatNormalizer().normalize(callTree);
    }

    public void print(PrintWriter writer) {
        print(writer, new DefaultComparator());
    }

    public void print(PrintWriter writer, Comparator comparator) {
        Collections.sort(methodEntries, comparator);
        List<Map<COLUMN, String>> rows = createRowValueList();
        Map<COLUMN, Integer> columnSizeMap = calculateColumnSize(rows);
        writeHeader(writer, columnSizeMap);
        writeRows(writer, rows, columnSizeMap);
        writer.flush();
    }

    private void writeRows(PrintWriter writer, List<Map<COLUMN, String>> rows, Map<COLUMN, Integer> columnSizeMap) {
        COLUMN[] columns = COLUMN.values();
        int columnNum = columns.length;
        StringBuilder rowFormatBuff = new StringBuilder();
        for (int i = 0; i < columnNum; i++) {
            if (i > 0) {
                rowFormatBuff.append(SP);
            }
            int columnSize = columnSizeMap.get(columns[i]);
            rowFormatBuff.append(String.format(columns[i].format, columnSize));
        }
        String rowFormat = rowFormatBuff.append("%n").toString();
        for (Map<COLUMN, String> row : rows) {
            Object[] rowValues = new String[columnNum];
            for (int i = 0; i < columnNum; i++) {
                rowValues[i] = row.get(columns[i]);
            }
            writer.printf(rowFormat, rowValues);
        }
    }

    private void writeHeader(PrintWriter writer, Map<COLUMN, Integer> columnSizeMap) {
        COLUMN[] columns = COLUMN.values();
        int columnNum = columns.length;
        StringBuilder headerFormatBuff = new StringBuilder();
        for (int i = 0; i < columnNum; i++) {
            if (i > 0) {
                headerFormatBuff.append(SP);
            }
            int columnSize = columnSizeMap.get(columns[i]);
            headerFormatBuff.append(String.format("%%-%ds", columnSize));
        }
        String headerFormat = headerFormatBuff.append("%n").toString();
        Object[] headerValues = new String[columnNum];
        for (int i = 0; i < columnNum; i++) {
            headerValues[i] = columns[i].name;
        }
        writer.printf(headerFormat, headerValues);
    }

    private Map<COLUMN, Integer> calculateColumnSize(List<Map<COLUMN, String>> rows) {
        Map<COLUMN, Integer> colSizeMap = new HashMap();
        for (COLUMN col : COLUMN.values()) {
            colSizeMap.put(col, col.name.length());
        }
        for (Map<COLUMN, String> row : rows) {
            for (COLUMN col : COLUMN.values()) {
                colSizeMap.put(col, Math.max(colSizeMap.get(col), row.get(col).length()));
            }
        }
        return colSizeMap;
    }

    private List<Map<COLUMN, String>> createRowValueList() {
        List<Map<COLUMN, String>> rows = new ArrayList(methodEntries.size());
        for (ProfileMethodEntry methodEntry : methodEntries) {
            Map<COLUMN, String> row = new HashMap();
            row.put(COLUMN.TIME_PERCENT, String.format("%.2f", methodEntry.getPercent()));
            row.put(COLUMN.TIME_TOTAL, String.format("%.2f", methodEntry.getTime().milliseconds()));
            row.put(COLUMN.CALLS, String.format("%d", methodEntry.getCallEntries().size()));
            row.put(COLUMN.TIME_MIN, String.format("%.2f", methodEntry.getMinTime().milliseconds()));
            row.put(COLUMN.TIME_MAX, String.format("%.2f", methodEntry.getMaxTime().milliseconds()));
            row.put(COLUMN.TIME_AVG, String.format("%.2f", methodEntry.getTimePerCall().milliseconds()));
            row.put(COLUMN.METHOD_NAME, methodEntry.getMethodName());
            row.put(COLUMN.CLASS_NAME, methodEntry.getClassName());
            rows.add(row);
        }
        return rows;
    }

    public enum COLUMN {

        TIME_PERCENT("%", "%%%ds"),
        CALLS("calls", "%%%ds"),
        TIME_TOTAL("total ms", "%%%ds"),
        TIME_AVG("ms/call", "%%%ds"),
        TIME_MIN("min ms", "%%%ds"),
        TIME_MAX("max ms", "%%%ds"),
        METHOD_NAME("method", "%%-%ds"),
        CLASS_NAME("class", "%%-%ds");

        private String name;
        private String format;

        COLUMN(String name, String format) {
            this.name = name;
            this.format = format;
        }
    }

    static class DefaultComparator implements Comparator<ProfileMethodEntry> {

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
