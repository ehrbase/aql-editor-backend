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

import org.ehrbase.aqleditor.dto.aql.orderby.OrderByExpressionDto;
import org.ehrbase.client.aql.containment.Containment;
import org.ehrbase.client.aql.orderby.OrderByExpression;

import java.util.List;
import java.util.Map;

public class OrderByBinder {

  private final SelectBinder selectBinder = new SelectBinder();

  public OrderByExpression bind(
      List<OrderByExpressionDto> dtoList, Map<Integer, Containment> containmentMap) {

    OrderByExpression orderByExpression = bind(dtoList.get(0), containmentMap);
    for (int i = 1; i < dtoList.size(); i++) {
      orderByExpression = orderByExpression.andThen(bind(dtoList.get(i), containmentMap));
    }
    return orderByExpression;
  }

  private OrderByExpression bind(
      OrderByExpressionDto dto, Map<Integer, Containment> containmentMap) {
    final OrderByExpression orderByExpression;

    switch (dto.getSymbol()) {
      case ASC:
        orderByExpression =
            OrderByExpression.ascending(selectBinder.bind(dto.getStatement(), containmentMap));
        break;
      case DESC:
        orderByExpression =
            OrderByExpression.descending(selectBinder.bind(dto.getStatement(), containmentMap));
        break;
      default:
        throw new RuntimeException();
    }
    return orderByExpression;
  }
}
