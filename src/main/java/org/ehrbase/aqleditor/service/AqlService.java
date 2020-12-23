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
import org.apache.commons.lang3.tuple.Pair;
import org.ehrbase.aql.binder.AqlBinder;
import org.ehrbase.aql.dto.AqlDto;
import org.ehrbase.aql.dto.condition.ConditionComparisonOperatorDto;
import org.ehrbase.aql.dto.condition.ConditionDto;
import org.ehrbase.aql.dto.condition.ConditionLogicalOperatorDto;
import org.ehrbase.aql.dto.condition.ParameterValue;
import org.ehrbase.aql.parser.AqlToDtoParser;
import org.ehrbase.aqleditor.dto.aql.Result;
import org.ehrbase.client.aql.query.EntityQuery;
import org.ehrbase.client.aql.record.Record;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AqlService {

  private static final AqlBinder aqlBinder = new AqlBinder();

  public Result buildAql(AqlDto aqlDto) {

    Pair<EntityQuery<Record>, List<ParameterValue>> pair = aqlBinder.bind(aqlDto);

    return new Result(
            pair.getLeft().buildAql(),
            pair.getRight().stream()
                    .collect(Collectors.toMap(ParameterValue::getName, ParameterValue::getType)));
  }

  public AqlDto parseAql(Result result) {
    AqlDto aqlDto = new AqlToDtoParser().parse(result.getQ());
    if (result.getQueryParameters() != null) {
      List<ParameterValue> parameterValues = extractParameterValues(aqlDto.getWhere());
      parameterValues.forEach(
              p -> {
                if (result.getQueryParameters().containsKey(p.getName())) {
                  p.setType(result.getQueryParameters().get(p.getName()));
                }
              });
    }
    return aqlDto;
  }

  private List<ParameterValue> extractParameterValues(ConditionDto conditionDto) {
    List<ParameterValue> values = new ArrayList<>();

    if (conditionDto instanceof ConditionComparisonOperatorDto) {
      if (((ConditionComparisonOperatorDto) conditionDto).getValue() instanceof ParameterValue) {
        values.add((ParameterValue) ((ConditionComparisonOperatorDto) conditionDto).getValue());
      }
    } else if (conditionDto instanceof ConditionLogicalOperatorDto) {
      values.addAll(
              ((ConditionLogicalOperatorDto) conditionDto)
                      .getValues().stream()
                      .map(this::extractParameterValues)
                      .flatMap(List::stream)
                      .collect(Collectors.toList()));
    }

    return values;
  }
}
