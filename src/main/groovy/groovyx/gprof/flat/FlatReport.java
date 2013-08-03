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

import groovyx.gprof.CallTree;
import groovyx.gprof.Report;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class FlatReport extends Report {

    public FlatReport(CallTree callTree) {
        super(callTree);
    }
    
    @Override
    public void prettyPrint(Map args, PrintWriter writer) {
        boolean separateThread =
                args.get("separateThread") instanceof Boolean ?
                        (Boolean) args.get("separateThread") : false;
        
        FlatReportNormalizer normalizer = new FlatReportNormalizer();
        normalizer.setSeparateThread(separateThread);
        List<FlatReportThreadElement> elements = normalizer.normalize(callTree);
        FlatReportPrinter printer = new FlatReportPrinter();
        printer.setSeparateThread(separateThread);
        printer.print(elements, writer);
    }

}
