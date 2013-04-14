
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

profile(includeMethods: ["java.util.*List.*"], excludeMethods: [ "*.ctor" ]) {
    def list
    list = new LinkedList()
    list << String.valueOf(true)
    list << String.valueOf(1)
    list << String.valueOf('a')
    list = new ArrayList(list)
    list[0]
    list[1]
    list[2]
}.prettyPrint()