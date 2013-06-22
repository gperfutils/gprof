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

public class ProxyReport extends Report {

    private Report delegate;

    public ProxyReport(CallTree callTree) {
        super(callTree);
    }

    public Report getFlat() {
        delegate = new FlatReport(callTree);
        return this;
    }

    public Report getCallGraph() {
        delegate = new CallGraphReport(callTree);
        return this;
    }

    @Override
    public ReportPrinter getPrinter() {
        return delegate.getPrinter();
    }

    @Override
    public ReportNormalizer getNormalizer() {
        return delegate.getNormalizer();
    }

}
