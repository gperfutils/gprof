package gprof

import org.junit.Test
import static org.junit.Assert.*

class ProfilerTest {

    String sleep100() {
        Thread.sleep(100)
    }
    String sleep200() {
        Thread.sleep(200)
    }

    @Test void defaultRun() {
        new Profiler().run {
            sleep100()
            sleep200()
        }.prettyPrint()
    }

}