package groovyx.gprof

import groovyx.gprof.flat.FlatReportNormalizer
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

}
