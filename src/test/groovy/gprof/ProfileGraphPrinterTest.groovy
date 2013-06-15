package gprof

import org.junit.Test

@Mixin(Mock)
class ProfileGraphPrinterTest {

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
