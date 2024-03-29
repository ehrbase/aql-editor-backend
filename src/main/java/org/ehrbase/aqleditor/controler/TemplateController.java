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

package org.ehrbase.aqleditor.controler;

import java.util.List;
import lombok.AllArgsConstructor;
import org.ehrbase.aqleditor.dto.template.TemplateDto;
import org.ehrbase.aqleditor.service.TemplateService;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        path = "/rest/v1/template",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class TemplateController extends BaseController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<List<TemplateDto>> getAll() {
        return ResponseEntity.ok(templateService.getAll());
    }

    @GetMapping(path = "{template_Id}")
    public ResponseEntity<WebTemplate> get(@PathVariable(value = "template_Id") String templateId) {

        return ResponseEntity.ok(templateService.getWebTemplate(templateId));
    }
}
