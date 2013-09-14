/*
 * Copyright 2013 Masato Nagai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gprof

import spock.lang.Specification

class CallMatcherTest extends Specification {

    def "pattern does not include wildcards"() {
        expect:
        def matcher = new CallMatcher("java.lang.String.getClass")
        matcher.match(text) == matched
        
        where:
        text  | matched
        "java.lang.String.getClass" | true
        "java.lang.String.getClassLoader" | false
    }

    def "pattern includes '*' wildcard(s)"() {
        expect:
        def matcher = new CallMatcher(pattern)
        matcher.match(text) == accepted
        
        where:
        pattern | text | accepted
        "*" | "java.lang.String.ctor" | true
        "*" | "" | true
        "****" | "java.lang.String.ctor" | true
        "****" | "" | true
        "java.*" | "java.lang.String.ctor" | true
        "java.*" | "javax.lang.String.ctor" | false
        "java.*" | "xjava.lang.String.ctor" | false
        "*.ctor" | "java.lang.String.ctor" | true
        "*.ctor" | "java.lang.String.size" | false
        "*.ctor" | "java.lang.String.ctorx" | false
        "java.*.ctor" | "java.lang.String.ctor" | true
        "java.*.ctor" | "java.lang.String.size" | false
        "java.*.ctor" | "javax.lang.String.ctor" | false
        "*.lang.*" | "java.lang.String.ctor" | true
        "*.lang.*" | "java.util.List.ctor" | false
        "*.*.*" | "java.lang.String.ctor" | true
        "*.*.*" | "Foo.ctor" | false
    }

    def "pattern includes '?' wildcard(s)"() {
        expect:
        def matcher = new CallMatcher(pattern)
        matcher.match(text) == accepted
        
        where:
        pattern | text | accepted
        "?" | "A" | true
        "?" | "" | false
        "??" | "AA" | true
        "??" | "A" | false
        "?.a" | "A.a" | true
        "?.a" | "AA.a" | false
        "A.?" | "A.a" | true
        "A.?" | "A.aa" | false
        "?.?" | "A.a" | true
        "?.?" | "A.aa" | false
    }

}
