package gprof

import org.junit.Test
import static org.junit.Assert.*

class ProfilerTest {

    private void doSomething() {
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
    }

    @Test void defaultRun() {
        new Profiler().run {
            doSomething()
        }.prettyPrint()
    }

}