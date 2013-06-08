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

        List<Map<Column, String>> lines = new ArrayList();
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
                        Column.SELF_TIME_PERCENT,
                            String.format("%.1f", 100f),
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
                            Column.SELF_TIME_PERCENT,
                                String.format("%.1f", child.getTimePercent()),
                            Column.SELF_TIME,
                                String.format("%.2f", childRef.getSelfTime().microseconds()),
                            Column.CHILDREN_TIME,
                                String.format("%.2f", childRef.getChildrenTime().microseconds()),
                            Column.CALLS,
                                String.format("%d", childRef.getMethod().getCallEntries().size()),
                            Column.NAME,
                                String.format("%s.%s [%d]",
                                    childRef.getMethod().getClassName(), childRef.getMethod().getClassName(), child.getIndex())));
                }
            }
        }

        Map<Column, Integer> columnWidth = new HashMap();
        for (Column col : Column.values()) {
            columnWidth.put(col, 0);
        }
        for (Map<Column, String> line : lines) {
            for (Column col : line.keySet()) {
                columnWidth.put(col, Math.max(columnWidth.get(col), line.get(col).length()));
            }
        }

        for (Map<Column, String> line : lines) {
            for (Column col : Column.values()) {
                int w = columnWidth.get(col);
                String format;
                if (line == header) {
                    format = "%-" + w + "s";
                } else {
                    switch(col) {
                        case TOTAL_TIME_PERCENT:
                        case SELF_TIME_PERCENT:
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
                line.put(col, String.format(format, line.get(col)));
            }
        }

        String separator = "  ";
        List<Column> columns =
            Arrays.asList(
                Column.INDEX, Column.TOTAL_TIME_PERCENT, Column.SELF_TIME_PERCENT,
                Column.SELF_TIME, Column.CHILDREN_TIME, Column.CALLS, Column.NAME);
        for (Map<Column, String> line: lines) {
            List vs = new ArrayList(columns.size());
            for (Column col : columns) {
                vs.add(line.get(col));
            }
            writer.println(ProfileCollections.join(vs, separator));
        }
        writer.flush();
    }

    public static enum Column {

        INDEX("index"),
        TOTAL_TIME_PERCENT("% time"),
        SELF_TIME_PERCENT("% self"),
        SELF_TIME("self"),
        CHILDREN_TIME("children"),
        CALLS("calls"),
        NAME("name");

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
