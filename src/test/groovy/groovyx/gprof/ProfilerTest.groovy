package groovyx.gprof

import groovyx.gprof.flat.FlatReportNormalizer
import org.codehaus.groovy.reflection.ClassInfo
import spock.lang.Specification

@Mixin(TestHelper)
class ProfilerTest extends Specification {

    def flatten(CallTree callTree) {
        return new FlatReportNormalizer().normalize(callTree)
    }
    
    def "Reports time is zero when profiling is stopped before the method call has not been finished"() {
        when:
        def p = new Profiler()
        p.start()
        Thread.start {
            Thread.sleep(300)
        }
        p.stop()

        then:
        flatten(p.report.callTree)
            .find { true }
            .methodElements
            .find { e -> e.method == method("java.lang.Thread", "sleep") }
            .time == 0
    }

    def "Start and stop profiling"() {
        def p = new Profiler()
        p.start()
        Thread.sleep(1)
        p.stop()
        boolean b = false
        flatten(p.report.callTree)
            .find { true }
            .methodElements
            .find { e ->
            if (e.method == method(Thread.class.name, "sleep") &&
                e.calls == 1) {
                b = true
                return false
            }
            return true
        }
        assert b
    }

    def "Restart profiling"() {
        def p = new Profiler()
        p.start()
        Thread.sleep(1)
        p.stop()
        p.start()
        Thread.sleep(1)
        p.stop()
        boolean b = false
        flatten(p.report.callTree)
            .find { true }
            .methodElements
            .find { e ->
            if (e.method == method(Thread.class.name, "sleep") &&
                e.calls == 2) {
                b = true
                return false
            }
            return true
        }
        assert b
    }

    def "Reset profiling"() {
        def p = new Profiler()
        p.start()
        Thread.sleep(1)
        p.stop()
        p.reset()
        p.start()
        Thread.sleep(1)
        p.stop()
        boolean b = false
        flatten(p.report.callTree)
            .find { true }
            .methodElements
            .find { e ->
            if (e.method == method(Thread.class.name, "sleep") &&
                e.calls == 1) {
                b = true
                return false
            }
            return true
        }
        assert b
    }
    
    def "error equals or less than 10 percent"() {
        setup:
        // warm up the profiled method
        10000.each {
            Thread.sleep(1)
        }
        def times = 100
        
        when:
        def prof = new Profiler()
        prof.start()
        times.each {
            Thread.sleep(1)
        }
        prof.stop()

        then:
        def actual =
            flatten(prof.report.callTree)
                .find { true }
                .methodElements
                .find { e -> e.method.className == Thread.class.name && e.method.methodName == "sleep" }
                .selfTimePerCall 
        def expected = (1..times).collect {
            def s = System.nanoTime()
            Thread.sleep(1)
            def time = System.nanoTime() - s
            time
        }.sum()/times
        Math.abs(actual - expected) < expected * 0.1 // 10 %
    }
    
    def "takes modified expandos from the original meta class"() {
        when:
            String.metaClass.static.abc = { "ABC" }
            def b = false
            profile {
                b = String.metaClass.methods.find { it.name == "abc" } != null
            }
        
        then:
            b
    }
    
    def "pass modified expandos to the original meta class"() {
        when:
            profile {
                String.metaClass.static.abc = { "ABC" }
            }
            def b = String.metaClass.methods.find { it.name == "abc" } != null
        
        then:
            b
        
    }

}
