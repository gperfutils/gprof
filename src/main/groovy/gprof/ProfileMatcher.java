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

public class ProfileMatcher {

    private char[] pattern;

    public ProfileMatcher(String pattern) {
        this.pattern = (pattern + '\0').toCharArray();
    }

    public boolean match(String text) {
        char[] textChars = (text + '\0').toCharArray();
        int patternIndex = 0;
        int textIndex = 0;
        while (true) {
            char patternChar = pattern[patternIndex++];
            if (patternChar == '\0') {
                if (textChars[textIndex] != '\0') {
                    // The text has more character but the pattern reached \0
                    return false;
                }
                // Completed.
                return true;
            } else if (patternChar == '*') {
                // Skip unmatched characters.
                while (true) {
                    patternChar = pattern[patternIndex];
                    if (patternChar == '\0') {
                        // Completed.
                        return true;
                    }
                    if (patternChar == '*' || patternChar == '?') {
                        break;
                    }
                    while (true) {
                        char textChar = textChars[textIndex++];
                        if (textChar == '\0') {
                            // The pattern requires more character but the text reached the \0.
                            return false;
                        }
                        if (textChar == patternChar) {
                            break;
                        }
                    }
                    patternIndex++;
                }
            } else if (patternChar == '?') {
                char textChar = textChars[textIndex++];
                if (textChar == '\0') {
                    // The pattern requires more character but the text reached the \0.
                    return false;
                }
                // Otherwise, just skip the character.
            } else {
                char textChar = textChars[textIndex++];
                if (textChar == '\0') {
                    // The pattern requires more character but the text reached the \0.
                    return false;
                }
                if (textChar != patternChar) {
                    return false;
                }
            }
        }
    }

    @Override
    public String toString() {
        return new String(pattern);
    }
}
