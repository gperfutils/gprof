package gprof

import spock.lang.Specification

@Mixin(TestHelper)
class CallGraphReportPrinterTest extends Specification {
    
    def report(args = [:], tree) {
        def out = new StringWriter()
        def r = new CallGraphReport(tree)
        r.prettyPrint(args, new PrintWriter(out))
        out.toString()
    }
    
    def "Prints a method call"() {
        when:
        def out = report(tree(
                methodCallNode("A", "a", 1000)
            ))
        
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
        def out = report(tree(
            methodCallNode("A", "a", 4000,
                methodCallNode("A", "b", 1250),
                methodCallNode("A", "c", 1750),
            )
        ))

        then:
        def expected = '''\
index  % time  self  children  calls  name             
               1.00      3.00    1/1      <spontaneous>
[1]     100.0  1.00      3.00      1  A.a [1]          
               1.75      0.00    1/1      A.c [2]      
               1.25      0.00    1/1      A.b [3]      
-------------------------------------------------------
               1.75      0.00    1/1      A.a [1]      
[2]      43.7  1.75      0.00      1  A.c [2]          
-------------------------------------------------------
               1.25      0.00    1/1      A.a [1]      
[3]      31.2  1.25      0.00      1  A.b [3]          
-------------------------------------------------------
'''
        out == expected
    }
    
    def "Prints a method call which has multiple callers"() {
        when:
        def out = report(tree(
            methodCallNode("A", "a", 1500,
                methodCallNode("A", "b", 500)),
            methodCallNode("A", "c", 2500,
                methodCallNode("A", "b", 500),
                methodCallNode("A", "b", 500))
        ))
        
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
        def out = report(tree(
            methodCallNode("A", "a", 1000 + 4500,
                methodCallNode("A", "b", 1000 + 500 + 3000,
                    methodCallNode("A", "c", 500),
                    methodCallNode("A", "b", 1000 + 500 + 1500,
                        methodCallNode("A", "c", 500),
                        methodCallNode("A", "b", 1000 + 500,
                            methodCallNode("A", "c", 500)))))))
        
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
        def out = report(tree(
                methodCallNode("A", "a", 1000+250+2750,
                    methodCallNode("A", "c", 250),
                    methodCallNode("A", "b", 500+250+2000,
                        methodCallNode("A", "c", 250),
                        methodCallNode("A", "a", 1000+250+750,
                            methodCallNode("A", "c", 250),
                            methodCallNode("A", "b", 500+250,
                                methodCallNode("A", "c", 250)))))))
        
        then:
        def expected ='''\
index  % time  self  children  calls  name                    
[1]     100.0  3.00      1.00    1+3  <cycle 1 as a whole> [1]
               2.00      0.50      2      A.a <cycle 1> [2]   
--------------------------------------------------------------
               3.00      1.00    1/1      <spontaneous>       
                                   1      A.b <cycle 1> [3]   
[2]      62.5  2.00      0.50      2  A.a <cycle 1> [2]       
                                   2      A.b <cycle 1> [3]   
               0.50      0.00    2/4      A.c [4]             
--------------------------------------------------------------
                                   2      A.a <cycle 1> [2]   
[3]      37.5  1.00      0.50      2  A.b <cycle 1> [3]       
                                   1      A.a <cycle 1> [2]   
               0.50      0.00    2/4      A.c [4]             
--------------------------------------------------------------
               0.50      0.00    2/4      A.a <cycle 1> [2]   
               0.50      0.00    2/4      A.b <cycle 1> [3]   
[4]      25.0  1.00      0.00      4  A.c [4]                 
--------------------------------------------------------------
'''
        out == expected  
    }
    
    def "Prints mix of cycle and recursion calls"() {
        when:
        def out = report(tree(
            methodCallNode("A", "a", 1000 + 1000 + (500 + 500 + 1000),
                methodCallNode("A", "a", 1000), // recursion in A.a
                methodCallNode("A", "b", 500 + 500 + 1000, // cycle between A.a and A.b
                    methodCallNode("A", "b", 500), // recursion in A.b
                    methodCallNode("A", "a", 1000)) // cycle between A.a and A.b
        )))
        
        then:
        def expected ='''\
index  % time  self  children  calls  name                    
[1]     100.0  4.00      0.00    1+2  <cycle 1 as a whole> [1]
               3.00      0.00    2+1      A.a <cycle 1> [2]   
--------------------------------------------------------------
               4.00      0.00    1/1      <spontaneous>       
                                   1      A.b <cycle 1> [3]   
[2]      75.0  3.00      0.00    2+1  A.a <cycle 1> [2]       
                                   1      A.b <cycle 1> [3]   
--------------------------------------------------------------
                                   1      A.a <cycle 1> [2]   
[3]      25.0  1.00      0.00    1+1  A.b <cycle 1> [3]       
                                   1      A.a <cycle 1> [2]   
--------------------------------------------------------------
'''
        out == expected
    }
    
    def "Prints multi-thread calls"() {
        when:
        def out = report(tree(
            methodCallNode("A", "a", 1000),
            methodCallNode("A", "b", 500),
            threadRunNode("Ta", 2,
                methodCallNode("A", "a", 1000),
                methodCallNode("A", "c", 250),
            ),
            threadRunNode("Tb", 3,
                methodCallNode("A", "b", 500),
                methodCallNode("A", "c", 250),
            )
        ))
        
        then:
        def expected ='''\
index  % time  self  children  calls  name             
               2.00      0.00    2/2      <spontaneous>
[1]      57.1  2.00      0.00      2  A.a [1]          
-------------------------------------------------------
               1.00      0.00    2/2      <spontaneous>
[2]      28.5  1.00      0.00      2  A.b [2]          
-------------------------------------------------------
               0.50      0.00    2/2      <spontaneous>
[3]      14.2  0.50      0.00      2  A.c [3]          
-------------------------------------------------------
'''
        out == expected
    }
    
    def "Prints multi-thread calls (separated)"() {
        when:
        def out = report(
            separateThread: true, 
            tree(
                methodCallNode("A", "a", 1000),
                methodCallNode("A", "b", 500),
                threadRunNode("Ta", 2,
                    methodCallNode("A", "a", 1000),
                    methodCallNode("A", "c", 250),
                ),
                threadRunNode("Tb", 3,
                    methodCallNode("A", "b", 500),
                    methodCallNode("A", "c", 250),
                )
            ))

        then:
        def expected ='''\
#1 main

index  % time  self  children  calls  name             
               1.00      0.00    1/1      <spontaneous>
[1]      66.6  1.00      0.00      1  A.a [1]          
-------------------------------------------------------
               0.50      0.00    1/1      <spontaneous>
[2]      33.3  0.50      0.00      1  A.b [2]          
-------------------------------------------------------
=======================================================
#2 Ta

index  % time  self  children  calls  name             
               1.00      0.00    1/1      <spontaneous>
[1]      80.0  1.00      0.00      1  A.a [1]          
-------------------------------------------------------
               0.25      0.00    1/1      <spontaneous>
[2]      20.0  0.25      0.00      1  A.c [2]          
-------------------------------------------------------
=======================================================
#3 Tb

index  % time  self  children  calls  name             
               0.50      0.00    1/1      <spontaneous>
[1]      66.6  0.50      0.00      1  A.b [1]          
-------------------------------------------------------
               0.25      0.00    1/1      <spontaneous>
[2]      33.3  0.25      0.00      1  A.c [2]          
-------------------------------------------------------
=======================================================
'''
        out == expected
    }

}
