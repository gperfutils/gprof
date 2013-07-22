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
package gprof.flat;

import gprof.ReportPrinter;

import java.io.PrintWriter;
import java.util.*;

import static gprof.flat.FlatReportPrinter.COLUMN.CALLS;

public class FlatReportPrinter implements ReportPrinter<FlatReportElement> {

    private static String SP = "  ";

    public void print(List<FlatReportElement> elements, PrintWriter writer) {
        print(elements, writer, new DefaultComparator());
    }

    public void print(List<FlatReportElement> elements, PrintWriter writer, Comparator comparator) {
        Collections.sort(elements, comparator);
        List<Map<COLUMN, String>> rows = createRowValueList(elements);
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
            COLUMN column = columns[i];
            columnSizeMap.get(columns[i]);
            int columnSize = columnSizeMap.get(column);
            switch (column) {
                case CALLS:
                case TIME_TOTAL:
                case TIME_MAX:
                case TIME_MIN:
                case TIME_AVG:
                    headerFormatBuff.append(String.format("%%%ds", columnSize));
                    break;
                default:
                    headerFormatBuff.append(String.format("%%-%ds", columnSize));
                    break;
            }
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

    private List<Map<COLUMN, String>> createRowValueList(List<FlatReportElement> elements) {
        List<Map<COLUMN, String>> rows = new ArrayList(elements.size());
        for (FlatReportElement element : elements) {
            Map<COLUMN, String> row = new HashMap();
            row.put(COLUMN.TIME_PERCENT, String.format("%.2f", element.getTimePercent()));
            row.put(COLUMN.TIME_TOTAL, String.format("%.2f", element.getTime().milliseconds()));
            row.put(CALLS, String.format("%d", element.getCalls()));
            row.put(COLUMN.TIME_MIN, String.format("%.2f", element.getMinTime().milliseconds()));
            row.put(COLUMN.TIME_MAX, String.format("%.2f", element.getMaxTime().milliseconds()));
            row.put(COLUMN.TIME_AVG, String.format("%.2f", element.getTimePerCall().milliseconds()));
            row.put(COLUMN.METHOD_NAME, element.getMethod().getMethodName());
            row.put(COLUMN.CLASS_NAME, element.getMethod().getClassName());
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

    static class DefaultComparator implements Comparator<FlatReportElement> {

        @Override
        public int compare(FlatReportElement o1, FlatReportElement o2) {
            int r = -(o1.getTime().compareTo(o2.getTime()));
            if (r == 0) {
                r = -(((Long) o1.getCalls()).compareTo(o2.getCalls()));
                if (r == 0) {
                    r = o1.getMethod().getClassName().compareTo(o2.getMethod().getClassName());
                    if (r == 0) {
                        r = o1.getMethod().getMethodName().compareTo(o2.getMethod().getMethodName());
                    }
                }
            }
            return r;
        }
    }

}
