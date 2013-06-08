package gprof

import org.junit.Test

class ProfileGraphPrinterTest {

    ProfileCallTree.Node nodeMock(ProfileEntry entry) {
        new ProfileCallTree.Node(entry)
    }

    ProfileCallTree.Node threadNodeMock(threadName) {
        def thread = new ProfileThreadEntry(new Thread(threadName))
        nodeMock(thread)
    }

    ProfileCallTree.Node methodNodeMock(className, methodName, long time) {
        def call = new ProfileCallEntry(className, methodName)
        call.time = new ProfileTime(time)
        nodeMock(call)
    }

    @Test void print() {
        ProfileCallTree callTree = new ProfileCallTree(Thread.currentThread());

        // multiple calls
        def na11 = methodNodeMock("class-100", "method-111", 100);
        def na12 = methodNodeMock("class-100", "method-111", 100);
        def na1 = methodNodeMock("class-10", "method-11", 100);
        na1.addChild(na11)
        na1.addChild(na12)

        def na21 = methodNodeMock("class-100", "method-121", 100);
        def na22 = methodNodeMock("class-100", "method-122", 100);
        def na2 = methodNodeMock("class-10", "method-12", 100);
        na2.addChild(na21)
        na2.addChild(na22)
        def na = threadNodeMock("thread-a");
        na.addChild(na1)
        na.addChild(na2)
        callTree.root.addChild(na)

        new ProfileGraphPrinter(callTree).print(new PrintWriter(System.out))
    }
}
