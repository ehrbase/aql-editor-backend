/*
 *
 * Copyright (c) 2020  Stefan Spiska (Vitasystems GmbH) and Hannover Medical School
 * This file is part of Project EHRbase
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.ehrbase.aqleditor.service;

import lombok.AllArgsConstructor;
import org.ehrbase.aqleditor.aql.binder.AqlBinder;
import org.ehrbase.aqleditor.dto.aql.AqlDto;
import org.ehrbase.aqleditor.dto.aql.Result;
import org.ehrbase.client.aql.query.EntityQuery;
import org.ehrbase.client.aql.record.Record;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AqlService {

  private static final AqlBinder aqlBinder = new AqlBinder();

  public Result buildAql(AqlDto aqlDto) {

    EntityQuery<Record> query = aqlBinder.bind(aqlDto);

    return new Result(query.buildAql());
  }
}
