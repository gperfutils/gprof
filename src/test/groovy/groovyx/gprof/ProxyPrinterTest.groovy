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
package groovyx.gprof

import spock.lang.Specification

@Mixin(TestHelper)
class ProxyPrinterTest extends Specification {
    
    def str(tree) {
        def out = new StringWriter()
        new ProxyReport(tree).prettyPrint(new PrintWriter(out))
        out.toString()
    }

    def "Prints mixed report"() {
        when:
        def out = str(tree(
            methodCallNode("A", "a", 1000)
        ))

        then:
        def expected = """\
Flat:

%       calls  total ms  ms/call   min ms   max ms  method  class
100.00      1   1000.00  1000.00  1000.00  1000.00  a       A    

Call graph:

index  % time  self  children  calls  name             
               1.00      0.00    1/1      <spontaneous>
[1]     100.0  1.00      0.00      1  A.a [1]          
-------------------------------------------------------
"""
        out == expected
        
    }

}
