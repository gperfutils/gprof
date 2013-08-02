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
package groovyx.gprof.flat

import groovyx.gprof.TestHelper
import spock.lang.Specification

@Mixin(TestHelper)
class FlatReportPrinterTest extends Specification {

    def report(args = [:], tree) {
        def out = new StringWriter()
        def r = new FlatReport(tree)
        r.prettyPrint(args, new PrintWriter(out))
        out.toString()
    }

    def "Prints a method call"() {
        when:
        def out = report(tree(
            methodCallNode("A", "a", 100)
        ))

        then:
        def expected = '''\
 %     cumulative   self            self     total    self     total    self     total       
time    seconds    seconds  calls  ms/call  ms/call  ms(min)  ms(min)  ms(max)  ms(max)  name
100.0        0.10     0.10      1   100.00   100.00   100.00   100.00   100.00   100.00  A.a 
'''
        out == expected
    }
    
    def "Prints a method call which has child calls"() {
        when:
        def out = report(tree(
            methodCallNode("A", "a", 400,
                methodCallNode("A", "b", 125),
                methodCallNode("A", "c", 175)
            )
        ))

        then:
        def expected = '''\
 %    cumulative   self            self     total    self     total    self     total       
time   seconds    seconds  calls  ms/call  ms/call  ms(min)  ms(min)  ms(max)  ms(max)  name
43.7        0.17     0.17      1   175.00   175.00   175.00   175.00   175.00   175.00  A.c 
31.2        0.30     0.12      1   125.00   125.00   125.00   125.00   125.00   125.00  A.b 
25.0        0.40     0.10      1   100.00   400.00   100.00   400.00   100.00   400.00  A.a 
'''
        out == expected
    }
    
    def "Prints multiple method calls"() {
        when:
        def out = report(tree(
            methodCallNode("A", "a", 430,
                methodCallNode("A", "b", 135),
                methodCallNode("A", "c", 185)),
            methodCallNode("A", "a", 400,
                methodCallNode("A", "b", 125),
                methodCallNode("A", "c", 175)),
            methodCallNode("A", "a", 370,
                methodCallNode("A", "b", 115),
                methodCallNode("A", "c", 165)),
        ))

        then:
        def expected = '''\
 %    cumulative   self            self     total    self     total    self     total       
time   seconds    seconds  calls  ms/call  ms/call  ms(min)  ms(min)  ms(max)  ms(max)  name
43.7        0.52     0.52      3   175.00   175.00   165.00   165.00   185.00   185.00  A.c 
31.2        0.90     0.37      3   125.00   125.00   115.00   115.00   135.00   135.00  A.b 
25.0        1.20     0.30      3   100.00   400.00    90.00   370.00   110.00   430.00  A.a 
'''
        out == expected
    }
    
}
