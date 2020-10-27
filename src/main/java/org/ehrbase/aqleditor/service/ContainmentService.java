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

import com.nedap.archie.rm.archetyped.Locatable;
import com.nedap.archie.rm.composition.EventContext;
import com.nedap.archie.rm.datavalues.quantity.DvInterval;
import com.nedap.archie.rminfo.ArchieRMInfoLookup;
import com.nedap.archie.rminfo.RMTypeInfo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.ehrbase.aqleditor.dto.ContainmentDto;
import org.ehrbase.aqleditor.dto.FieldDto;
import org.ehrbase.webtemplate.model.WebTemplate;
import org.ehrbase.webtemplate.model.WebTemplateNode;
import org.ehrbase.webtemplate.parser.FlatPath;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

@Service
@AllArgsConstructor
public class ContainmentService {

  public static final ArchieRMInfoLookup ARCHIE_RM_INFO_LOOKUP = ArchieRMInfoLookup.getInstance();

  private TemplateService templateService;

  public ContainmentDto buildContainment(String templateId) {

    WebTemplate webTemplate = templateService.getWebTemplate(templateId);
    Context context = new Context();

    handleNext(context, webTemplate.getTree());

    return context.containmentQueue.getFirst();
  }

  private void handleNext(Context context, WebTemplateNode childNode) {
    if (visitChildren(childNode)) {
      if (context.containmentQueue.isEmpty()
          || (childNode.getNodeId() != null && !childNode.getNodeId().startsWith("at"))) {
        ContainmentDto containmentDto = new ContainmentDto();
        containmentDto.setArchetypeId(childNode.getNodeId());
        if (!context.containmentQueue.isEmpty()) {
          context.containmentQueue.peek().getChildren().add(containmentDto);
        }
        context.containmentQueue.push(containmentDto);
        context.aqlQueue.push(childNode.getAqlPath());
        context.nodeQueue.push(childNode);
        childNode.getChildren().forEach(n -> handleNext(context, n));
        context.nodeQueue.remove();
        context.aqlQueue.remove();
        if (context.containmentQueue.size() > 1) {
          context.containmentQueue.remove();
        }
      }
    } else {
      FieldDto fieldDto = new FieldDto();
      fieldDto.setName(childNode.getName());
      fieldDto.setRmType(childNode.getRmType());
      String relativAql = StringUtils.removeStart(childNode.getAqlPath(), context.aqlQueue.peek());
      fieldDto.setAqlPath(new FlatPath(relativAql).format(false));
      context.nodeQueue.push(childNode);
      fieldDto.setHumanReadablePath(buildHumanReadablePath(context));
      context.containmentQueue.peek().getFields().add(fieldDto);
      context.nodeQueue.remove();
    }
  }

  private String buildHumanReadablePath(Context context) {
    StringBuilder sb = new StringBuilder();
    for (Iterator<WebTemplateNode> iterator = context.nodeQueue.descendingIterator();
        iterator.hasNext(); ) {
      WebTemplateNode node = iterator.next();
      sb.append(node.getId());
      if (iterator.hasNext()) {
        sb.append("/");
      }
    }
    return sb.toString();
  }

  protected boolean visitChildren(WebTemplateNode node) {
    RMTypeInfo typeInfo = ARCHIE_RM_INFO_LOOKUP.getTypeInfo(node.getRmType());
    return typeInfo != null
        && (Locatable.class.isAssignableFrom(typeInfo.getJavaClass())
            || EventContext.class.isAssignableFrom(typeInfo.getJavaClass())
            || DvInterval.class.isAssignableFrom(typeInfo.getJavaClass()));
  }

  private class Context {
    Deque<WebTemplateNode> nodeQueue = new ArrayDeque<>();
    Deque<ContainmentDto> containmentQueue = new ArrayDeque<>();
    Deque<String> aqlQueue = new ArrayDeque<>();
  }
}
