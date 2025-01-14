/**
 * Copyright © 2016-2023 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.mqtt.broker.common.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public abstract class SearchTextBased extends BaseData {

    private static final long serialVersionUID = -539812997348227609L;

    public SearchTextBased() {
        super();
    }

    public SearchTextBased(UUID id) {
        super(id);
    }

    public SearchTextBased(SearchTextBased searchTextBased) {
        super(searchTextBased);
    }

    @JsonIgnore
    public abstract String getSearchText();

}
