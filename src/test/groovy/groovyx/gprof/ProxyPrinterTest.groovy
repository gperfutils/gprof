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
    
    def "Prints using the default writer"() {
        setup:
        def os = new ByteArrayOutputStream()
        def sysout = System.out
        System.out = new PrintStream(os)
        
        when:
        new ProxyReport(tree( 
            methodCallNode("A", "a", 100)
        )).prettyPrint()
        
        then:
        def expected = """\
Flat:

 %     cumulative   self            self     total    self    total   self    total      
time    seconds    seconds  calls  ms/call  ms/call  min ms  min ms  max ms  max ms  name
100.0        0.10     0.10      1   100.00   100.00  100.00  100.00  100.00  100.00  A.a 

Call graph:

index  % time  self  children  calls  name             
               0.10      0.00    1/1      <spontaneous>
[1]     100.0  0.10      0.00      1  A.a [1]          
-------------------------------------------------------
"""
        os.toString() == expected
        
        cleanup:
        System.out = sysout
    }

    def "Prints mixed report"() {
        when:
        def out = str(tree(
            methodCallNode("A", "a", 100)
        ))

        then:
        def expected = """\
Flat:

 %     cumulative   self            self     total    self    total   self    total      
time    seconds    seconds  calls  ms/call  ms/call  min ms  min ms  max ms  max ms  name
100.0        0.10     0.10      1   100.00   100.00  100.00  100.00  100.00  100.00  A.a 

Call graph:

index  % time  self  children  calls  name             
               0.10      0.00    1/1      <spontaneous>
[1]     100.0  0.10      0.00      1  A.a [1]          
-------------------------------------------------------
"""
        out == expected
        
    }

}
