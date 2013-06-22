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
package gprof;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

class Utils {

    static HashMap hashMap(Object...kvs) {
        HashMap hashMap = new HashMap();
        for (int i = 0, n = kvs.length; i < n; i += 2) {
            hashMap.put(kvs[i], kvs[i + 1]);
        }
        return hashMap;
    }

    static String join(Collection coll, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Iterator it = coll.iterator();;) {
            sb.append(it.next().toString());
            if (it.hasNext()) {
                sb.append(separator);
            } else {
                break;
            }
        }
        return sb.toString();
    }

}
