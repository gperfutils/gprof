package groovyx.gprof

import groovyx.gprof.flat.FlatReportNormalizer
import org.codehaus.groovy.reflection.ClassInfo
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import spock.lang.Specification

@Mixin(TestHelper)
class ProfilerTest extends Specification {
    
    def flatten(CallTree callTree) {
        return new FlatReportNormalizer().normalize(callTree)
    }
    
    def setup() {
    }
    
    def cleanup() {
    }
    
    def clearMetaClass(Class klass) {
        GroovySystem.metaClassRegistry.removeMetaClass(klass)    
    }
    
    def clearMetaClass(Object obj) {
        GroovySystem.metaClassRegistry.setMetaClass(obj, null)
    }
    
    def "run with closure"() {
        when:
        def report = profile {
            Thread.sleep(1)
        }

        then:
        flatten(report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method.className == Thread.class.name && e.method.methodName == "sleep" }
    }
    
    def "start and stop"() {
        when:
        def p = new Profiler()
        p.start()
        Thread.sleep(1)
        p.stop()
        
        then:
        flatten(p.report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method.className == Thread.class.name && e.method.methodName == "sleep" }
    }

    def "reuse data when restarted"() {
        when:
        def p = new Profiler()
        p.start()
        Thread.sleep(1)
        p.stop()
        p.start()
        Thread.sleep(1)
        p.stop()
        
        then:
        flatten(p.report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method.className == Thread.class.name && e.method.methodName == "sleep" }
            .calls == 2
    }

    def "clears data when reset"() {
        when:
        def p = new Profiler()
        p.start()
        Thread.sleep(1)
        p.stop()
        p.reset()
        p.start()
        Thread.sleep(1)
        p.stop()
        
        then:
        flatten(p.report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method.className == Thread.class.name && e.method.methodName == "sleep" }
            .calls == 1
    }

    def "keeps an expando static method which is added before profiling"() {
        when:
        String.metaClass.static.abc = { "ABC" }

        then:
        def r = false
        profile {
            r = String.abc() == "ABC"
        }
        r
        
        cleanup:
        clearMetaClass(String)
    }

    def "keeps an expando instance method which is added before profiling"() {
        when:
        String.metaClass.abc = { "ABC" }
        
        then:
        def r = false
        profile {
            r = new String().abc() == "ABC"
        }
        r
        
        cleanup:
        clearMetaClass(String)
    }
    
    def "keeps an expando property which is added before profiling"() {
        when:
        String.metaClass.abc = "ABC"

        then:
        def r
        profile {
            r = new String().abc == "ABC"
        }
        r

        cleanup:
        clearMetaClass(String)
    }
    
    def "profiles an expando method whith is added to a variable before profiling"() {
        when:
        def s = new String()
        s.metaClass.abc = { "ABC" }
        def report = profile {
            s.abc()
        }

        then:
        flatten(report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method.className == String.class.name && e.method.methodName == "abc" }
    }
    
    def "profiles an expando instance method which is added to the caller before profiling"() {
        when:
        this.metaClass.abc = { "ABC" }
        def report = profile {
            abc()
        }

        then:
        flatten(report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method.methodName == "abc" }
        
        cleanup:
        clearMetaClass(this)
    }
    
    def "profiles 'call' in expando method which is added to the caller before profiling"() {
        when:
        def clos = { nth ->
            if (nth < 2) nth
            else call(nth - 1) + call(nth - 2) }
        this.metaClass.fib = clos
        def report = profile {
            fib(3)
        }

        then:
        flatten(report.callTree)
            .find { true }
            .methodElements
            .find { it.method.className == clos.class.name && it.method.methodName == "call" }
            .calls == 2
        
        cleanup:
        clearMetaClass(this)
    }
    
    def "profiles 'call' in expando static method which is added to the caller before profiling"() {
        when:
        def clos = { nth ->
            if (nth < 2) nth
            else call(nth - 1) + call(nth - 2) }
        this.metaClass.static.fib = clos
        def report = profile {
            fib(3)
        }

        then:
        flatten(report.callTree)
            .find { true }
            .methodElements
            .find { it.method.className == clos.class.name && it.method.methodName == "call" }
            .calls == 2
        
        cleanup:
        clearMetaClass(this)
    }
    
    def "profiles an expando instance method which is added while profiling"() {
        when:
        def report = profile {
            String.metaClass.abc = { "ABC" }
            new String().abc()
        }

        then:
        flatten(report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method.className == String.class.name && e.method.methodName == "abc" }
        
        cleanup:
        clearMetaClass(String)
    }

    def "profiles an expando static method which is added while profiling"() {
        when:
        def report = profile {
            String.metaClass.static.abc = { "ABC" }
            String.abc()
        }

        then:
        flatten(report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method.className == String.class.name && e.method.methodName == "abc" }
        
        cleanup:
        clearMetaClass(String)

    }
    
    def "profiles 'call' in expando instance method which is added while profiling"() {
        when:
        def report = profile {
            def obj = new String()
            obj.metaClass.fib = { nth ->
                if (nth < 2) nth
                else call(nth - 1) + call(nth - 2)
            }
            obj.fib(2)
        }

        then:
        flatten(report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method.methodName == "call" }
            .calls == 2
    }

    def "profiles 'call' in expando static method which is added while profiling"() {
        when:
        def report = profile {
            Math.metaClass.static.fib = { nth ->
                if (nth < 2) nth
                else call(nth - 1) + call(nth - 2)
            }
            Math.fib(2)
        }
        
        then:
        flatten(report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method.methodName == "call" }
            .calls == 2
        
        cleanup:
        clearMetaClass(Math)
    }
    
    def "remains an expando static method which is added while profiling"() {
        when:
        profile {
            String.metaClass.static.abc = { "ABC" }
        }

        then:
        String.abc() == "ABC"

        cleanup:
        clearMetaClass(String)
    }
    
    def "remains an expando instance method which is added while profiling"() {
        when:
        profile {
            String.metaClass.abc = { "ABC" }
        }
        
        then:
        new String().abc() == "ABC"

        cleanup:
        clearMetaClass(String)
    }

    def "remains an expando property which is added while profiling"() {
        when:
        profile {
            String.metaClass.abc = "ABC"
        }

        then:
        new String().abc == "ABC"

        cleanup:
        clearMetaClass(String)
    }
    
}
