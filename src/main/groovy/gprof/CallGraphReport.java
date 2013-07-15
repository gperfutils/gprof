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
import java.util.List;

public class CallGraphReport extends Report {
    
    private List<CallGraphReportElement> elements;

    public CallGraphReport(CallTree callTree) {
        super(callTree);
        elements = new CallGraphReportNormalizer().normalize(callTree);
    }

    public void prettyPrint(PrintWriter writer) {
        new CallGraphReportPrinter().print(elements, writer); 
    }

}
