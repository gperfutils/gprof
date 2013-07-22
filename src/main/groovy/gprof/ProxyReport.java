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

import gprof.callgraph.CallGraphReport;
import gprof.flat.FlatReport;

import java.io.PrintWriter;
import java.util.Map;

public class ProxyReport extends Report {
    
    private Report flatReport;
    private Report callGraphReport;

    public ProxyReport(CallTree callTree) {
        super(callTree);
        flatReport = new FlatReport(callTree);
        callGraphReport = new CallGraphReport(callTree);
    }
    
    @Override
    public void prettyPrint(Map args, PrintWriter writer) {
        writer.println("Flat:");
        writer.println();
        flatReport.prettyPrint(args, writer);
        writer.println();
        writer.println("Call graph:");
        writer.println();
        callGraphReport.prettyPrint(args, writer);
    }

}
