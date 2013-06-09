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

public class ProfileGraphPrinter implements ProfilePrinter {

    private static Character SEPARATOR_CHAR = '-';
    private static String COLUMN_SEPARATOR = "  ";

    private List<ProfileGraphEntry> entries;

    public ProfileGraphPrinter(ProfileCallTree callTree) {
        entries = new ProfileGraphNormalizer().normalize(callTree);
    }

    @Override
    public void print(PrintWriter writer) {
        print(writer, null);
    }

    @Override
    public void print(PrintWriter writer, Comparator comparator) {
        Map<Long, ProfileGraphEntry> graphTable = new HashMap();
        for (ProfileGraphEntry entry : entries) {
            graphTable.put(entry.getIndex(), entry);
        }

        List lines = new ArrayList();
        Map<Column, String> header = new HashMap();
        for (Column col : Column.values()) {
            header.put(col, col.toString());
        }
        lines.add(header);
        for (ProfileGraphEntry entry : entries) {
            if (entry.getDepth() == 0 || !entry.getChildren().isEmpty()) {
                lines.add(
                    ProfileCollections.hashMap(
                        Column.INDEX,
                            String.format("[%d]", entry.getIndex()),
                        Column.TOTAL_TIME_PERCENT,
                            String.format("%.1f", entry.getTimePercent()),
                        Column.SELF_TIME,
                            String.format("%.2f", entry.getSelfTime().microseconds()),
                        Column.CHILDREN_TIME,
                            String.format("%.2f", entry.getChildrenTime().microseconds()),
                        Column.CALLS,
                            String.format("%d", entry.getMethod().getCallEntries().size()),
                        Column.NAME,
                            String.format("%s.%s [%d]",
                                entry.getMethod().getClassName(), entry.getMethod().getMethodName(), entry.getIndex())));

                for (ProfileGraphEntry.Child child : entry.getChildren()) {
                    ProfileGraphEntry childRef = graphTable.get(child.getIndex());
                    lines.add(
                        ProfileCollections.hashMap(
                            Column.INDEX,
                                "",
                            Column.TOTAL_TIME_PERCENT,
                                "",
                            Column.SELF_TIME,
                                String.format("%.2f", childRef.getSelfTime().microseconds()),
                            Column.CHILDREN_TIME,
                                String.format("%.2f", childRef.getChildrenTime().microseconds()),
                            Column.CALLS,
                                String.format("%d", childRef.getMethod().getCallEntries().size()),
                            Column.NAME,
                                String.format("    %s.%s [%d]",
                                    childRef.getMethod().getClassName(), childRef.getMethod().getClassName(), child.getIndex())));
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
                writer.println(ProfileCollections.join(vs, "  "));
            } else if (line == SEPARATOR_CHAR) {
                writer.println(separator);
            }
        }
        writer.flush();
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
