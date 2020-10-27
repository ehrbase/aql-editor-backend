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

import org.ehrbase.aqleditor.dto.TemplateDto;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TemplateService {

  private TestDataTemplateProvider testDataTemplateProvider = new TestDataTemplateProvider();

  public List<TemplateDto> getAll() {
    return testDataTemplateProvider.listTemplateIds().stream()
            .map(testDataTemplateProvider::find)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(this::map)
            .collect(Collectors.toList());
  }

  private TemplateDto map(OPERATIONALTEMPLATE operationaltemplate) {
    TemplateDto templateDto = new TemplateDto();
    templateDto.setTemplateId(operationaltemplate.getTemplateId().getValue());
    templateDto.setDescription(
            operationaltemplate.getDescription().getDetailsArray()[0].getPurpose());
    return templateDto;
  }
}
