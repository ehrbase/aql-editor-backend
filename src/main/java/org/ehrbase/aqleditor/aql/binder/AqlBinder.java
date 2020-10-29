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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ehrbase.aqleditor.dto.aql.AqlDto;
import org.ehrbase.aqleditor.dto.aql.condition.ParameterValue;
import org.ehrbase.client.aql.condition.Condition;
import org.ehrbase.client.aql.containment.Containment;
import org.ehrbase.client.aql.containment.ContainmentExpression;
import org.ehrbase.client.aql.field.EhrFields;
import org.ehrbase.client.aql.field.SelectAqlField;
import org.ehrbase.client.aql.query.EntityQuery;
import org.ehrbase.client.aql.query.Query;
import org.ehrbase.client.aql.record.Record;
import org.ehrbase.client.aql.top.Direction;
import org.ehrbase.client.aql.top.TopExpresion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AqlBinder {

  private final SelectBinder selectBinder = new SelectBinder();
  private final ContainmentBinder containmentBinder = new ContainmentBinder();
  private final WhereBinder whereBinder = new WhereBinder();

  public Pair<EntityQuery<Record>, List<ParameterValue>> bind(AqlDto aqlDto) {
    Pair<ContainmentExpression, Map<Integer, Containment>> pair =
        containmentBinder.buildContainment(aqlDto.getContains());
    ArrayList<ParameterValue> parameterValues = new ArrayList<>();
    if (aqlDto.getEhr() != null) {
      pair.getRight().put(aqlDto.getEhr().getContainmentId(), EhrFields.EHR_CONTAINMENT);
    }

    SelectAqlField<Object>[] selectAqlFields =
        aqlDto.getSelect().getStatement().stream()
            .map(s -> selectBinder.bind(s, pair.getRight()))
            .toArray(SelectAqlField[]::new);

    EntityQuery<Record> query = Query.buildEntityQuery(pair.getLeft(), selectAqlFields);
    if (aqlDto.getWhere() != null) {
      Pair<Condition, List<ParameterValue>> conditionPair =
          whereBinder.bind(aqlDto.getWhere(), pair.getRight());
      query.where(conditionPair.getLeft());
      parameterValues.addAll(conditionPair.getRight());
    }
    if (Direction.FORWARD.equals(aqlDto.getSelect().getTopDirection())) {
      query.top(TopExpresion.forward(aqlDto.getSelect().getTopCount()));
    } else if (Direction.BACKWARD.equals(aqlDto.getSelect().getTopDirection())) {
      query.top(TopExpresion.backward(aqlDto.getSelect().getTopCount()));
    }

    return new ImmutablePair<>(query, parameterValues);
  }
}
