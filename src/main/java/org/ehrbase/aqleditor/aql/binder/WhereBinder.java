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

import org.ehrbase.aqleditor.dto.aql.condition.ConditionComparisonOperatorDto;
import org.ehrbase.aqleditor.dto.aql.condition.ConditionDto;
import org.ehrbase.aqleditor.dto.aql.condition.SimpleValue;
import org.ehrbase.client.aql.condition.Condition;
import org.ehrbase.client.aql.containment.Containment;

import java.util.Map;

public class WhereBinder {
  private final SelectBinder selectBinder = new SelectBinder();

  public Condition bind(ConditionDto dto, Map<Integer, Containment> containmentMap) {
    Condition condition;
    if (dto instanceof ConditionComparisonOperatorDto) {
      switch (((ConditionComparisonOperatorDto) dto).getSymbol()) {
        case EQ:
          condition =
              Condition.equal(
                  selectBinder.bind(
                      ((ConditionComparisonOperatorDto) dto).getStatement(), containmentMap),
                  bindValue((SimpleValue) ((ConditionComparisonOperatorDto) dto).getValue()));
          break;
        default:
          throw new RuntimeException();
      }
    } else {
      throw new RuntimeException();
    }
    return condition;
  }



  private Object bindValue(SimpleValue value) {
    return value.getValue();
  }

}
