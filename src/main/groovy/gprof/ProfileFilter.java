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

import java.util.ArrayList;
import java.util.List;

public class ProfileFilter {

    private List<ProfileMatcher> includes = new ArrayList();
    private List<ProfileMatcher> excludes = new ArrayList();

    public void addInclude(String pattern) {
        this.includes.add(new ProfileMatcher(pattern));
    }

    public void addIncludes(List<String> patterns) {
        for (String pattern : patterns) {
            addInclude(pattern);
        }
    }

    public void addExclude(String pattern) {
        this.excludes.add(new ProfileMatcher(pattern));
    }

    public void addExcludes(List<String> patterns) {
        for (String pattern : patterns) {
            addExclude(pattern);
        }
    }

    public boolean accept(String text) {
        if (!includes.isEmpty()) {
            boolean included = false;
            for (ProfileMatcher include : includes) {
                if (include.match(text)) {
                    included = true;
                    break;
                }
            }
            if (!included) {
                return false;
            }
        }
        if (!excludes.isEmpty()) {
            boolean excluded = false;
            for (ProfileMatcher exclude : excludes) {
                if (exclude.match(text)) {
                    excluded = true;
                    break;
                }
            }
            if (excluded) {
                return false;
            }
        }
        return true;
    }
}
