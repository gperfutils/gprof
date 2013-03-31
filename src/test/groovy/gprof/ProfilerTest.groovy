package gprof

import org.junit.Test

import java.util.concurrent.Callable

import static org.junit.Assert.*

class ProfilerTest {

    String sleep100() {
        Thread.sleep(100)
    }
    String sleep200() {
        Thread.sleep(200)
    }

    @Test void defaultRun() {
        profile {
            sleep100()
            sleep200()
        }.prettyPrint()
    }

}