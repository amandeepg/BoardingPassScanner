/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ncredinburgh.iata.specs;

public enum FormatCode {
    SINGLE("S", "Single"),
    MULTIPLE("M", "Multiple"),
    UNKNOWN("", "<unknown>");

    private final String value;
    private final String description;

    FormatCode(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static FormatCode parse(CharSequence s) {
        for (FormatCode code : values()) {
            if (code.getValue().equals(s)) {
                return code;
            }
        }
        return UNKNOWN;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
