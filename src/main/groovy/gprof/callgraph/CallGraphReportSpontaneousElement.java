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
package gprof.callgraph;

import gprof.MethodInfo;

public class CallGraphReportSpontaneousElement extends CallGraphReportMethodElement {
    
    static final MethodInfo NON_METHOD = new MethodInfo("", "");
    
    public CallGraphReportSpontaneousElement() {
        super(0, NON_METHOD);
        setCalls(1);
    }

}
