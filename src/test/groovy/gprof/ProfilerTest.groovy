package gprof

import org.junit.Test
import static org.junit.Assert.*

class ProfilerTest {

    @Test void run() {
        /*
        def gprof = new Profiler()
        gprof.observe(StringBuffer).observe(StringBuilder)
        */
        (new Profiler() << StringBuffer << StringBuilder).run {
            def sb
            sb = new StringBuilder()
            sb.append('foo')
            sb.append('bar')
            sb.append('baz')
            sb.toString()
            sb = new StringBuffer()
            sb.append('foo')
            sb.append('bar')
            sb.append('baz')
            sb.toString()
        }.prettyPrint()
    }

}