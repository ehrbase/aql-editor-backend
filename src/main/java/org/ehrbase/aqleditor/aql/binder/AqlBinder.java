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

package org.ehrbase.aqleditor.aql.binder;

import org.apache.commons.lang3.tuple.Pair;
import org.ehrbase.aqleditor.dto.aql.AqlDto;
import org.ehrbase.client.aql.containment.Containment;
import org.ehrbase.client.aql.containment.ContainmentExpression;
import org.ehrbase.client.aql.field.EhrFields;
import org.ehrbase.client.aql.field.SelectAqlField;
import org.ehrbase.client.aql.query.EntityQuery;
import org.ehrbase.client.aql.query.Query;
import org.ehrbase.client.aql.record.Record;

import java.util.Map;

public class AqlBinder {

  private final SelectBinder selectBinder = new SelectBinder();
    private final ContainmentBinder containmentBinder = new ContainmentBinder();

    public EntityQuery<Record> bind(AqlDto aqlDto) {
    Pair<ContainmentExpression, Map<Integer, Containment>> pair =
            containmentBinder.buildContainment(aqlDto.getContains());

    if (aqlDto.getEhr() != null) {
      pair.getRight().put(aqlDto.getEhr().getContainmentId(), EhrFields.EHR_CONTAINMENT);
    }

    SelectAqlField<Object>[] selectAqlFields =
        aqlDto.getSelect().getStatement().stream()
            .map(s -> selectBinder.bind(s, pair.getRight()))
            .toArray(SelectAqlField[]::new);

    EntityQuery<Record> query = Query.buildEntityQuery(pair.getLeft(), selectAqlFields);
    return query;
  }

}
