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
package groovyx.gprof.flat;

import groovyx.gprof.ReportPrinter;

import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.*;

import static groovyx.gprof.flat.FlatReportPrinter.COLUMN.CALLS;

public class FlatReportPrinter implements ReportPrinter<FlatReportMethodElement> {

    private static String SP = "  ";

    @Override
    public void print(List<FlatReportMethodElement> elements, PrintWriter writer) {
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
                case CUMULATIVE_TIME:
                case SELF_TIME:
                case CALLS:
                case SELF_TIME_PER_CALL:
                case TOTAL_TIME_PER_CALL:
                case SELF_MIN_TIME:
                case TOTAL_MIN_TIME:
                case SELF_MAX_TIME:
                case TOTAL_MAX_TIME:
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
            headerValues[i] = columns[i].header1;
        }
        writer.printf(headerFormat, headerValues);
        for (int i = 0; i < columnNum; i++) {
            headerValues[i] = columns[i].header2;
        }
        writer.printf(headerFormat, headerValues);
    }

    private Map<COLUMN, Integer> calculateColumnSize(List<Map<COLUMN, String>> rows) {
        Map<COLUMN, Integer> colSizeMap = new HashMap();
        for (COLUMN col : COLUMN.values()) {
            colSizeMap.put(col, Math.max(col.header1.length(), col.header2.length()));
        }
        for (Map<COLUMN, String> row : rows) {
            for (COLUMN col : COLUMN.values()) {
                colSizeMap.put(col, Math.max(colSizeMap.get(col), row.get(col).length()));
            }
        }
        return colSizeMap;
    }

    private List<Map<COLUMN, String>> createRowValueList(List<FlatReportMethodElement> elements) {
        List<Map<COLUMN, String>> rows = new ArrayList(elements.size());
        for (FlatReportMethodElement element : elements) {
            Map<COLUMN, String> row = new HashMap();
            row.put(COLUMN.TIME_PERCENT, Formatter.percent(element.getTimePercent()));
            row.put(COLUMN.CUMULATIVE_TIME, Formatter.sec(element.getCumulativeTime()));
            row.put(COLUMN.SELF_TIME, Formatter.sec(element.getSelfTime()));
            row.put(CALLS, String.format("%d", element.getCalls()));
            row.put(COLUMN.SELF_TIME_PER_CALL, Formatter.msec(element.getSelfTimePerCall()));
            row.put(COLUMN.TOTAL_TIME_PER_CALL, Formatter.msec(element.getTimePerCall()));
            row.put(COLUMN.SELF_MIN_TIME, Formatter.msec(element.getMinSelfTime()));
            row.put(COLUMN.TOTAL_MIN_TIME, Formatter.msec(element.getMinTime()));
            row.put(COLUMN.SELF_MAX_TIME, Formatter.msec(element.getMaxSelfTime()));
            row.put(COLUMN.TOTAL_MAX_TIME, Formatter.msec(element.getMaxTime()));
            row.put(COLUMN.NAME, Formatter.name(element.getMethod().getClassName(), element.getMethod().getMethodName()));
            rows.add(row);
        }
        return rows;
    }

    public enum COLUMN {

        TIME_PERCENT(
                " %  ", 
                "time",
                "%%%ds"),
        CUMULATIVE_TIME(
                "cumulative",
                " seconds  ",
                "%%%ds"),
        SELF_TIME(
                " self  ",
                "seconds",
                "%%%ds"),
        CALLS(
                "     ",
                "calls",
                "%%%ds"),
        SELF_TIME_PER_CALL(
                " self  ",
                "ms/call",
                "%%%ds"),
        TOTAL_TIME_PER_CALL(
                " total ",
                "ms/call",
                "%%%ds"),
        SELF_MIN_TIME(
                " self  ",
                "ms(min)",
                "%%%ds"),
        TOTAL_MIN_TIME(
                " total ",
                "ms(min)",
                "%%%ds"),
        SELF_MAX_TIME(
                " self  ",
                "ms(max)",
                "%%%ds"),
        TOTAL_MAX_TIME(
                " total ",
                "ms(max)",
                "%%%ds"),
        NAME(
                "    ",
                "name",
                "%%-%ds"),
        ;

        private String header1;
        private String header2;
        private String format;

        COLUMN(String header1, String header2, String format) {
            this.header1 = header1;
            this.header2 = header2;
            this.format = format;
        }
    }
    
    private static class Formatter {

        private static Format TIME_PERCENT_FORMAT;
        private static Format TIME_FORMAT;
        static {
            DecimalFormat df;
            df = new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.DOWN);
            TIME_PERCENT_FORMAT = df;
            df = new DecimalFormat("0.00");
            df.setRoundingMode(RoundingMode.DOWN);
            TIME_FORMAT = df;
        }

        public static String sec(float ns) {
            return TIME_FORMAT.format(ns * 0.000000001);
        }
        
        public static String msec(float ns) {
            return TIME_FORMAT.format(ns * 0.000001);
        }

        public static String percent(double percent) {
            return TIME_PERCENT_FORMAT.format(percent);
        }

        public static String name(String className, String methodName) {
            return String.format("%s.%s", className, methodName);
        }

    }

}
