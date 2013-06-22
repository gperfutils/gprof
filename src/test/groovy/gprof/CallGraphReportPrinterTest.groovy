package gprof

import spock.lang.Specification

@Mixin(TestHelper)
class CallGraphReportPrinterTest extends Specification {

    def norm(tree) {
        new CallGraphReportNormalizer().normalize(tree)
    }

    def str(data) {
        def out = new StringWriter()
        new CallGraphReportPrinter().print(data, new PrintWriter(out))
        out.toString()
    }

    void "Prints in call graph"() {
        when:
        def out = str(norm(tree(
                methodCallNode("class-a", "method-1", 4000,
                    methodCallNode("class-b", "method-1", 1000),
                    methodCallNode("class-b", "method-1", 1000),
                    methodCallNode("class-c", "method-1", 1000),
                ),
                methodCallNode("class-a", "method-1", 4000,
                    methodCallNode("class-b", "method-1", 1000),
                    methodCallNode("class-b", "method-1", 1000),
                    methodCallNode("class-c", "method-1", 1000),
                ),
                methodCallNode("class-a", "method-2", 3000,
                    methodCallNode("class-a", "method-2", 2000,
                        methodCallNode("class-a", "method-2", 1000)
                    )
                ))))

        then:
        def expected = '''\
index  % time  self  children  calls  name                    
[1]      57.1  2.00      6.00      2  class-a.method-1 [1]    
               4.00      0.00      4      class-b.method-1 [3]
               2.00      0.00      2      class-c.method-1 [4]
--------------------------------------------------------------
[2]      42.9  6.00      0.00    1+2  class-a.method-2 [2]    
--------------------------------------------------------------
[3]      28.6  4.00      0.00      4  class-b.method-1 [3]    
--------------------------------------------------------------
[4]      14.3  2.00      0.00      2  class-c.method-1 [4]    
--------------------------------------------------------------
'''
        out == expected
    }
}
