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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.ehrbase.aqleditor.dto.template.TemplateDto;
import org.ehrbase.openehr.sdk.webtemplate.filter.Filter;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplate;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TemplateService {

    private static final TestDataTemplateProvider testDataTemplateProvider = new TestDataTemplateProvider();

    public List<TemplateDto> getAll() {
        return testDataTemplateProvider.listTemplateIds().stream()
                .map(testDataTemplateProvider::find)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::map)
                .collect(Collectors.toList());
    }

    public WebTemplate getWebTemplate(String templateId) {
        return testDataTemplateProvider
                .find(templateId)
                .map(o -> new OPTParser(o).parse())
                .map(w -> new Filter().filter(w))
                .orElse(null);
    }

    public WebTemplate getRawWebTemplate(String templateId) {
        return testDataTemplateProvider
                .find(templateId)
                .map(o -> new OPTParser(o).parse())
                .orElse(null);
    }

    private TemplateDto map(OPERATIONALTEMPLATE operationaltemplate) {
        TemplateDto templateDto = new TemplateDto();
        templateDto.setTemplateId(operationaltemplate.getTemplateId().getValue());
        templateDto.setDescription(
                operationaltemplate.getDescription().getDetailsArray()[0].getPurpose());
        return templateDto;
    }
}
