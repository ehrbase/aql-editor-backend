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

import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ehrbase.aqleditor.dto.aql.condition.ConditionComparisonOperatorDto;
import org.ehrbase.aqleditor.dto.aql.condition.ConditionDto;
import org.ehrbase.aqleditor.dto.aql.condition.ParameterValue;
import org.ehrbase.aqleditor.dto.aql.condition.SimpleValue;
import org.ehrbase.client.aql.condition.Condition;
import org.ehrbase.client.aql.containment.Containment;
import org.ehrbase.client.aql.field.SelectAqlField;
import org.ehrbase.client.aql.parameter.Parameter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WhereBinder {
  private final SelectBinder selectBinder = new SelectBinder();

  @SneakyThrows
  public Pair<Condition, List<ParameterValue>> bind(
      ConditionDto dto, Map<Integer, Containment> containmentMap) {
    Condition condition;
    List<ParameterValue> parameterList = new ArrayList<>();
    if (dto instanceof ConditionComparisonOperatorDto) {

      final Class valueClass;
      final Object value;
      if (((ConditionComparisonOperatorDto) dto).getValue() instanceof SimpleValue) {
        valueClass = Object.class;
        value = ((SimpleValue) ((ConditionComparisonOperatorDto) dto).getValue()).getValue();
      } else if (((ConditionComparisonOperatorDto) dto).getValue() instanceof ParameterValue) {
        valueClass = Parameter.class;
        value =
            new Parameter<>(
                ((ParameterValue) ((ConditionComparisonOperatorDto) dto).getValue()).getName());
        parameterList.add(((ParameterValue) ((ConditionComparisonOperatorDto) dto).getValue()));
      } else {
        throw new RuntimeException();
      }
      Method method =
          Condition.class.getMethod(
              ((ConditionComparisonOperatorDto) dto).getSymbol().getJavaName(),
              SelectAqlField.class,
              valueClass);
      condition =
          (Condition)
              method.invoke(
                  null,
                  selectBinder.bind(
                      ((ConditionComparisonOperatorDto) dto).getStatement(), containmentMap),
                  value);

    } else {
      throw new RuntimeException();
    }

    return new ImmutablePair<>(condition, parameterList);
  }
}
