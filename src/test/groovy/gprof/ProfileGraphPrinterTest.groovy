package gprof

import org.junit.Test

class ProfileGraphPrinterTest {

    def nodeMock(entry, Object... children) {
        def node = new ProfileCallTree.Node(entry)
        node.getChildren().addAll(children)
        node
    }

    def threadNodeMock(threadName, Object... children) {
        def thread = new ProfileThreadEntry(new Thread(threadName))
        nodeMock(thread, children)
    }

    def methodNodeMock(className, methodName, long time, Object... children) {
        def call = new ProfileCallEntry(className, methodName)
        call.time = new ProfileTime(time)
        nodeMock(call, children)
    }

    @Test void print() {
        ProfileCallTree callTree = new ProfileCallTree(Thread.currentThread());
        callTree.root.addChild(
            threadNodeMock("thread-a",
                methodNodeMock("class-a", "method-1", 4000,
                    methodNodeMock("class-b", "method-1", 1000),
                    methodNodeMock("class-b", "method-1", 1000),
                    methodNodeMock("class-c", "method-1", 1000),
                ),
                methodNodeMock("class-a", "method-1", 4000,
                    methodNodeMock("class-b", "method-1", 1000),
                    methodNodeMock("class-b", "method-1", 1000),
                    methodNodeMock("class-c", "method-1", 1000),
                ),
                methodNodeMock("class-a", "method-2", 3000,
                    methodNodeMock("class-a", "method-2", 2000,
                        methodNodeMock("class-a", "method-2", 1000)
                    )
                ),
            )
        )

        new ProfileGraphPrinter(callTree).print(new PrintWriter(System.out))
    }
}
