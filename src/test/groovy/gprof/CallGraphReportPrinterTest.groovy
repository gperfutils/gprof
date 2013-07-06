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
    
    def "Prints a method call"() {
        when:
        def out = str(norm(tree(
                methodCallNode("A", "a", 1000)
            )))
        
        then:
        def expected = '''\
index  % time  self  children  calls  name             
               1.00      0.00    1/1      <spontaneous>
[1]     100.0  1.00      0.00      1  A.a [1]          
-------------------------------------------------------
'''
        out == expected
    }
    
    def "Prints a method call which has child calls"() {
        when:
        def out = str(norm(tree(
            methodCallNode("A", "a", 4000,
                methodCallNode("A", "b", 1250),
                methodCallNode("A", "c", 1750),
            )
        )))

        then:
        def expected = '''\
index  % time  self  children  calls  name             
               1.00      3.00    1/1      <spontaneous>
[1]     100.0  1.00      3.00      1  A.a [1]          
               1.25      0.00    1/1      A.b [2]      
               1.75      0.00    1/1      A.c [3]      
-------------------------------------------------------
               1.25      0.00    1/1      A.a [1]      
[2]      31.2  1.25      0.00      1  A.b [2]          
-------------------------------------------------------
               1.75      0.00    1/1      A.a [1]      
[3]      43.7  1.75      0.00      1  A.c [3]          
-------------------------------------------------------
'''
        out == expected
    }
    
    def "Prints a method call which has multiple callers"() {
        when:
        def out = str(norm(tree(
            methodCallNode("A", "a", 1500,
                methodCallNode("A", "b", 500)),
            methodCallNode("A", "c", 2500,
                methodCallNode("A", "b", 500),
                methodCallNode("A", "b", 500))
        )))
        
        then:
        def expected = '''\
index  % time  self  children  calls  name             
               1.00      3.00    1/1      <spontaneous>
[1]     100.0  1.00      3.00      1  A.a [1]          
               0.50      0.00    1/3      A.b [2]      
-------------------------------------------------------
               0.50      0.00    1/3      A.a [1]      
               1.00      0.00    2/3      A.c [3]      
[2]      31.2  1.50      0.00      3  A.b [2]          
-------------------------------------------------------
               2.00      0.00    1/1      <spontaneous>
[3]      43.7  2.00      0.00      1  A.c [3]          
               1.00      0.00    2/3      A.b [2]      
-------------------------------------------------------
'''
    }
    
    def "Prints a recursive method calls"() {
        when:
        def out = str(norm(tree(
            methodCallNode("A", "a", 5500,
                methodCallNode("A", "b", 4500,
                    methodCallNode("A", "c", 500),
                    methodCallNode("A", "b", 3000,
                        methodCallNode("A", "c", 500),
                        methodCallNode("A", "b", 1500,
                            methodCallNode("A", "c", 500))))))))
        
        then:
        def expected = '''\
index  % time  self  children  calls  name             
               1.00      4.50    1/1      <spontaneous>
[1]     100.0  1.00      4.50      1  A.a [1]          
               3.00      1.50    1/1      A.b [2]      
-------------------------------------------------------
               3.00      1.50    1/1      A.a [1]      
[2]      81.8  3.00      1.50    1+2  A.b [2]          
               1.50      0.00    3/3      A.c [3]      
-------------------------------------------------------
               1.50      0.00    3/3      A.b [2]      
[3]      27.2  1.50      0.00      3  A.c [3]          
-------------------------------------------------------
'''
        out == expected
    }
    
    def "Prints cycle of recursive calls"() {
        when:
        def data = norm(tree(
                methodCallNode("A", "a", 1000+250+2750,
                    methodCallNode("A", "c", 250),
                    methodCallNode("A", "b", 500+250+2000,
                        methodCallNode("A", "c", 250),
                        methodCallNode("A", "a", 1000+250+750,
                            methodCallNode("A", "c", 250),
                            methodCallNode("A", "b", 500+250,
                                methodCallNode("A", "c", 250)))))))
        def out = str(data)
        
        then:
        def expected ='''\
index  % time  self  children  calls  name                    
               3.00      1.00    1/1      <spontaneous>       
                                   1      A.b <cycle 1> [3]   
[1]      62.5  2.00      0.50      2  A.a <cycle 1> [1]       
               0.50      0.00    2/4      A.c [2]             
                                   2      A.b <cycle 1> [3]   
--------------------------------------------------------------
               0.50      0.00    2/4      A.b <cycle 1> [3]   
               0.50      0.00    2/4      A.a <cycle 1> [1]   
[2]      25.0  1.00      0.00      4  A.c [2]                 
--------------------------------------------------------------
                                   2      A.a <cycle 1> [1]   
[3]      37.5  1.00      0.50      2  A.b <cycle 1> [3]       
               0.50      0.00    2/4      A.c [2]             
                                   1      A.a <cycle 1> [1]   
--------------------------------------------------------------
[4]     100.0  3.00      1.00    1+3  <cycle 1 as a whole> [4]
               2.00      0.50      2      A.a <cycle 1> [1]   
--------------------------------------------------------------
'''
        out == expected  
    }

}
