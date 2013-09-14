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

class CallFilterTest extends Specification {

    def "accept all"() {
        when:
        def filter = new CallFilter()
        
        then:
        filter.accept("abc")
    }

    def "pattern to include contains pattern to exclude"() {
        expect:
        def filter = new CallFilter()
        filter.addInclude("a*")
        filter.addExclude("ab*")
        filter.accept(text) == accepted
         
        where:
        text  | accepted
        "acb" | true
        "abc" | false
    }

    def "pattern to exclude contains pattern to include"() {
        expect:
        def filter = new CallFilter()
        filter.addInclude("ab*")
        filter.addExclude("a*")
        filter.accept(text) == accepted
        
        where:
        text  | accepted
        "acb" | false
        "abc" | false 
    }

}
