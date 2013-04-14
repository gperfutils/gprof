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
package gprof

import org.junit.Test

class ProfileMatcherTest {

    @Test void "does not use wildcards"() {
        def matcher = new ProfileMatcher("java.lang.String.getClass")
        assert matcher.match("java.lang.String.getClass")
        assert !matcher.match("java.lang.String.getClassLoader")
    }

    @Test void "use '*' wildcard"() {
        def matcher

        matcher = new ProfileMatcher("*")
        assert matcher.match("java.lang.String.ctor")
        assert matcher.match("")

        matcher = new ProfileMatcher("****")
        assert matcher.match("java.lang.String.ctor")
        assert matcher.match("")

        matcher = new ProfileMatcher("java.*")
        assert matcher.match("java.lang.String.ctor")
        assert !matcher.match("javax.lang.String.ctor")
        assert !matcher.match("xjava.lang.String.ctor")

        matcher = new ProfileMatcher("*.ctor")
        assert matcher.match("java.lang.String.ctor")
        assert !matcher.match("java.lang.String.size")
        assert !matcher.match("java.lang.String.ctorx")

        matcher = new ProfileMatcher("java.*.ctor")
        assert matcher.match("java.lang.String.ctor")
        assert !matcher.match("java.lang.String.size")
        assert !matcher.match("javax.lang.String.ctor")

        matcher = new ProfileMatcher("*.lang.*")
        assert matcher.match("java.lang.String.ctor")
        assert !matcher.match("java.util.List.ctor")

        matcher = new ProfileMatcher("*.*.*")
        assert matcher.match("java.lang.String.ctor")
        assert !matcher.match("Foo.ctor")
    }

    @Test void "use '?' wildcard"() {
        def matcher

        matcher = new ProfileMatcher("?")
        assert matcher.match("A")
        assert !matcher.match("")

        matcher = new ProfileMatcher("??")
        assert matcher.match("AA")
        assert !matcher.match("A")

        matcher = new ProfileMatcher("?.a")
        assert matcher.match("A.a")
        assert !matcher.match("AA.a")

        matcher = new ProfileMatcher("A.?")
        assert matcher.match("A.a")
        assert !matcher.match("A.aa")

        matcher = new ProfileMatcher("?.?")
        assert matcher.match("A.a")
        assert !matcher.match("A.aa")
    }

}
