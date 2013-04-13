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

class ProfileFilterTest {

    @Test void "accept all"() {
        def filter = new ProfileMethodFilter()
        assert filter.accept("abc")
        assert filter.accept("acb")
    }

    @Test void "pattern to include contains pattern to exclude"() {
        def filter = new ProfileMethodFilter()
        filter.addInclude("a*")
        filter.addExclude("ab*")
        assert filter.accept("acb")
        assert !filter.accept("abc")
    }

    @Test void "pattern to exclude contains pattern to include"() {
        def filter = new ProfileMethodFilter()
        filter.addInclude("ab*")
        filter.addExclude("a*")
        assert !filter.accept("acb")
        assert !filter.accept("abc")
    }

}
