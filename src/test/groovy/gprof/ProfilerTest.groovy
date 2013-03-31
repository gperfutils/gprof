package gprof

import org.junit.Test
import static org.junit.Assert.*

class ProfilerTest {
    String atoz() {
        StringBuffer sb = new StringBuffer();
        ('a'..'z').each { sb.append(it) }
        sb.toString()
    }
    String AtoZ() {
        StringBuffer sb = new StringBuffer();
        ('A'..'Z').each { sb.append(it) }
        sb.toString()
    }

    @Test void defaultRun() {
        new Profiler().run {
            atoz()
            AtoZ()
        }.prettyPrint()
    }

}