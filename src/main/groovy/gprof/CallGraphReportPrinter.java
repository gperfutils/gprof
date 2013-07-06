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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.*;

public class CallGraphReportPrinter implements ReportPrinter<CallGraphReportElement> {

    private static Character SEPARATOR_CHAR = '-';
    private static String COLUMN_SEPARATOR = "  ";
    private static String SPONTANEOUS = "<spontaneous>";
    
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
        
        public static String index(long index) {
            return String.format("[%d]", index);    
        }
        
        public static String cycleIndex(long number) {
            return String.format("<cycle %d>", number);    
        }
        
        public static String time(long nanotime) {
            return TIME_FORMAT.format(nanotime * 0.001);
        }

        public static String percent(double percent) {
            return TIME_PERCENT_FORMAT.format(percent);
        }
        
        public static String indent(String s) {
            return "    " + s;
        }
        
        public static String name(String className, String methodName, long index, long cycleIndex) {
            String s = String.format("%s.%s", className, methodName);
            if (cycleIndex > 0) {
                s += " " + cycleIndex(cycleIndex);
            }
            s += " " + index(index);
            return s;
        }
        
        public static String primaryCalls(long totalCalls, long recursiveCalls) {
            if (recursiveCalls > 0) {
                return String.format("%d+%d", totalCalls - recursiveCalls, recursiveCalls);
            }
            return String.format("%d", totalCalls);
        }
        
        public static String childCalls(long calls, long totalCalls) {
            return String.format("%d/%d", calls, totalCalls);
        }
        
        public static String cycleChildCalls(long calls) {
            return String.format("%d", calls);
        }
        
    }

    @Override
    public void print(List<CallGraphReportElement> elements, PrintWriter writer) {
        print(elements, writer, null);
    }

    @Override
    public void print(List<CallGraphReportElement> elements, PrintWriter writer, Comparator comparator) {
        for (CallGraphReportElement element : elements) {
            printElement(element.getSubElements(), writer, comparator);
        }
    }
    
    protected void printElement(Collection<CallGraphReportSubElement> elements, PrintWriter writer, Comparator comparator) {
        Map<Long, CallGraphReportSubElement> graphTable = new HashMap();
        for (CallGraphReportSubElement entry : elements) {
            graphTable.put(entry.getIndex(), entry);
        }

        List lines = new ArrayList();
        Map<Column, String> header = new HashMap();
        for (Column col : Column.values()) {
            header.put(col, col.toString());
        }
        lines.add(header);
        for (CallGraphReportSubElement element : elements) {
            if (element.getParents().isEmpty()) {
            } else {
                for (CallGraphReportSubElement.Parent parent : element.getParents().values()) {
                    if (parent.getIndex() == 0) {
                        lines.add(
                                Utils.hashMap(
                                        Column.INDEX,
                                        "",
                                        Column.TOTAL_TIME_PERCENT,
                                        "",
                                        Column.SELF_TIME,
                                        Formatter.time(parent.getSelfTime().nanoseconds()),
                                        Column.CHILDREN_TIME,
                                        Formatter.time(parent.getChildrenTime().nanoseconds()),
                                        Column.CALLS,
                                        Formatter.childCalls(
                                                parent.getCalls() - parent.getRecursiveCalls(),
                                                element.getCalls() - element.getRecursiveCalls() - element.getCycleCalls()),
                                        Column.NAME,
                                        Formatter.indent(SPONTANEOUS)
                                )
                            );
                    } else if (parent.getIndex() == element.getIndex()) {
                        // ignore recursive call
                    } else {
                        CallGraphReportSubElement parentRef = graphTable.get(parent.getIndex());
                        if (parentRef.getCycleIndex() > 0 && parentRef.getCycleIndex() == element.getCycleIndex()) {
                            lines.add(
                                    Utils.hashMap(
                                            Column.INDEX,
                                            "",
                                            Column.TOTAL_TIME_PERCENT,
                                            "",
                                            Column.SELF_TIME,
                                            "",
                                            Column.CHILDREN_TIME,
                                            "",
                                            Column.CALLS,
                                            Formatter.cycleChildCalls(parent.getCalls()),
                                            Column.NAME,
                                            Formatter.indent(Formatter.name(parentRef.getMethod().getClassName(),
                                                    parentRef.getMethod().getMethodName(),
                                                    parentRef.getIndex(),
                                                    parentRef.getCycleIndex()))));
                            
                        } else {
                            lines.add(
                                Utils.hashMap(
                                    Column.INDEX,
                                    "",
                                    Column.TOTAL_TIME_PERCENT,
                                    "",
                                    Column.SELF_TIME,
                                    Formatter.time(parent.getSelfTime().nanoseconds()),
                                    Column.CHILDREN_TIME,
                                    Formatter.time(parent.getChildrenTime().nanoseconds()),
                                    Column.CALLS,
                                    Formatter.childCalls(parent.getCalls() - parent.getRecursiveCalls(),
                                            element.getCalls() - element.getRecursiveCalls() - element.getCycleCalls()),
                                    Column.NAME,
                                    Formatter.indent(Formatter.name(
                                            parentRef.getMethod().getClassName(),
                                            parentRef.getMethod().getMethodName(),
                                            parentRef.getIndex(),
                                            parentRef.getCycleIndex()))
                                )
                            );
                        }
                    }
                }
            }
            lines.add(primaryLine(element));

            for (CallGraphReportSubElement.Child child : element.getChildren().values()) {
                CallGraphReportSubElement childRef = graphTable.get(child.getIndex());
                CallGraphReportSubElement.Parent childParent = childRef.getParents().get(element.getIndex());
                if (element instanceof CallGraphReportWholeCycleElement) {
                    lines.add(
                            Utils.hashMap(
                                    Column.INDEX,
                                    "",
                                    Column.TOTAL_TIME_PERCENT,
                                    "",
                                    Column.SELF_TIME,
                                    Formatter.time(childRef.getSelfTime().nanoseconds()),
                                    Column.CHILDREN_TIME,
                                    Formatter.time(childRef.getChildrenTime().nanoseconds()),
                                    Column.CALLS,
                                    Formatter.cycleChildCalls(childRef.getCalls()),
                                    Column.NAME,
                                    Formatter.indent(Formatter.name(
                                            childRef.getMethod().getClassName(),
                                            childRef.getMethod().getMethodName(),
                                            childRef.getIndex(),
                                            childRef.getCycleIndex()))));
                } else if (childRef.getCycleIndex() > 0 && childRef.getCycleIndex() == element.getCycleIndex()) {
                    lines.add(
                            Utils.hashMap(
                                    Column.INDEX,
                                    "",
                                    Column.TOTAL_TIME_PERCENT,
                                    "",
                                    Column.SELF_TIME,
                                    "",
                                    Column.CHILDREN_TIME,
                                    "",
                                    Column.CALLS,
                                    Formatter.cycleChildCalls(childParent.getCalls()),
                                    Column.NAME,
                                    Formatter.indent(Formatter.name(
                                            childRef.getMethod().getClassName(),
                                            childRef.getMethod().getMethodName(),
                                            childRef.getIndex(),
                                            childRef.getCycleIndex()))));
                    
                } else {
                    lines.add(
                        Utils.hashMap(
                                Column.INDEX,
                                "",
                                Column.TOTAL_TIME_PERCENT,
                                "",
                                Column.SELF_TIME,
                                Formatter.time(childParent.getSelfTime().nanoseconds()),
                                Column.CHILDREN_TIME,
                                Formatter.time(childParent.getChildrenTime().nanoseconds()),
                                Column.CALLS,
                                Formatter.childCalls(childParent.getCalls() - childParent.getRecursiveCalls(), childRef.getCalls() - childRef.getRecursiveCalls()),
                                Column.NAME,
                                Formatter.indent(Formatter.name(
                                        childRef.getMethod().getClassName(),
                                        childRef.getMethod().getMethodName(),
                                        childRef.getIndex(),
                                        childRef.getCycleIndex()))));
                }
            }
            lines.add(SEPARATOR_CHAR);
        }

        Map<Column, Integer> columnWidth = new HashMap();
        for (Column col : Column.values()) {
            columnWidth.put(col, 0);
        }
        for (Object line : lines) {
            if (line instanceof Map) {
                Map<Column, String> cols = (Map<Column, String>) line;
                for (Column col : cols.keySet()) {
                    columnWidth.put(col, Math.max(columnWidth.get(col), cols.get(col).length()));
                }
            }
        }
        int rowWidth = 0;
        for (int w : columnWidth.values()) {
            rowWidth += w;
        }
        rowWidth += COLUMN_SEPARATOR.length() * (columnWidth.values().size() - 2); // do not count Column.SPAN

        for (Object line : lines) {
            if (line instanceof Map) {
                Map<Column, String> cols = (Map<Column, String>) line;
                for (Column col : Column.values()) {
                    if (col == Column.SPAN) {
                        continue;
                    }
                    int w = columnWidth.get(col);
                    String format;
                    if (line == header) {
                        format = "%-" + w + "s";
                    } else {
                        switch(col) {
                            case TOTAL_TIME_PERCENT:
                            case SELF_TIME:
                            case CHILDREN_TIME:
                            case CALLS:
                                format = "%" + w + "s";
                                break;
                            default:
                                format = "%-" + w + "s";
                                break;
                        }
                    }
                    cols.put(col, String.format(format, cols.get(col)));
                }
            }
        }

        List<Column> columns =
            Arrays.asList(
                Column.INDEX, Column.TOTAL_TIME_PERCENT,
                Column.SELF_TIME, Column.CHILDREN_TIME, Column.CALLS, Column.NAME);
        String separator = createSeparator(rowWidth);

        for (Object line: lines) {
            if (line instanceof Map) {
                Map<Column, String> cols = (Map<Column, String>) line;
                List vs = new ArrayList(columns.size());
                for (Column col : columns) {
                    vs.add(cols.get(col));
                }
                writer.println(Utils.join(vs, "  "));
            } else if (line == SEPARATOR_CHAR) {
                writer.println(separator);
            }
        }
        writer.flush();
    }

    private HashMap primaryLine(CallGraphReportSubElement element) {
        String name;
        if (element instanceof CallGraphReportWholeCycleElement) {
            name = String.format("<cycle %d as a whole> " + Formatter.index(element.getIndex()), element.getCycleIndex());
        } else {
            name = Formatter.name(
                    element.getMethod().getClassName(),
                    element.getMethod().getMethodName(),
                    element.getIndex(),
                    element.getCycleIndex());
        }
        return Utils.hashMap(
                Column.INDEX,
                Formatter.index(element.getIndex()),
                Column.TOTAL_TIME_PERCENT,
                Formatter.percent(element.getTimePercent()),
                Column.SELF_TIME,
                Formatter.time(element.getSelfTime().nanoseconds()),
                Column.CHILDREN_TIME,
                Formatter.time(element.getChildrenTime().nanoseconds()),
                Column.CALLS,
                Formatter.primaryCalls(element.getCalls(), element.getRecursiveCalls()),
                Column.NAME,
                name);
    }

    private String createSeparator(int rowWidth) {
        StringBuilder separatorBuff = new StringBuilder();
        for (int i = 0; i < rowWidth; i++) {
            separatorBuff.append(SEPARATOR_CHAR);
        }
        return separatorBuff.toString();
    }

    public static enum Column {

        INDEX("index"),
        TOTAL_TIME_PERCENT("% time"),
        SELF_TIME("self"),
        CHILDREN_TIME("children"),
        CALLS("calls"),
        NAME("name"),
        SPAN(""); // FIXME ugly..

        private String name;

        Column(String name) {
           this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

}
