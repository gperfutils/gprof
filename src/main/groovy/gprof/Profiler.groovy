package gprof

class Profiler implements Interceptor {

    private List observables = []
    private Map prof = [ classes: [:] ]

    Profiler observe(Class observable) {
        observables << observable
        return this
    }

    Profiler leftShift(Class observable) {
        return observe(observable)
    }

    def run(Map opts = [:], Closure task) {
        def registry = GroovySystem.metaClassRegistry
        if (opts.observables) {
            observables.addAll(opts.observables)
        }
        observables.each { c ->
            def proxy = ProxyMetaClass.getInstance(c)
            proxy.interceptor = this
            registry.setMetaClass(c, proxy)
        }
        task()
        prof.time = 0L
        prof.classes.each { className, cprof ->
            cprof.time = 0L
            cprof.methods.each { methodName, mprof ->
                mprof.time = 0L
                mprof.calls.each { prof ->
                    mprof.time += prof.time
                }
                mprof.timePerCall = mprof.time / mprof.calls.size()
                cprof.time += mprof.time
            }
            prof.time += cprof.time
        }
        return new Profile(prof)
    }

    long stime
    long smem

    @Override
    Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        stime = System.nanoTime()
        smem = Runtime.runtime.totalMemory()
    }

    @Override
    Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        def className = object instanceof Class ? object.name : object.class.name
        if (!prof.classes[className]) {
            prof.classes[className] = [ methods: [:] ]
        }
        if (methodName == 'ctor') {
          methodName = '<init>'
        }
        def cprof = prof.classes[className]
        if (!cprof.methods[methodName]) {
            cprof.methods[methodName] = [ calls: [] ]
        }
        def mprof = cprof.methods[methodName]
        mprof.calls << [ time: System.nanoTime() - stime ]
        return result
    }

    @Override
    boolean doInvoke() {
        return true
    }
}
