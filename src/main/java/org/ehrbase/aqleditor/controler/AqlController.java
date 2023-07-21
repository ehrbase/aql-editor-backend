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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.ehrbase.aqleditor.dto.aql.Result;
import org.ehrbase.aqleditor.service.AqlService;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.ehrbase.openehr.sdk.aql.parser.serializer.AqlDtoSerializer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        path = "/rest/v1/aql",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class AqlController extends BaseController {

    private AqlService aqlService;

    @PostMapping
    public ResponseEntity<Result> buildAql(@RequestBody String aqlDto) throws JsonProcessingException {

        ObjectMapper objectMapper = AqlDtoSerializer.getObjectMapper();
        return ResponseEntity.ok(aqlService.buildAql(objectMapper.readValue(aqlDto, AqlQuery.class)));
    }

    @GetMapping
    public ResponseEntity<String> parseAql(@RequestBody Result result) throws JsonProcessingException {
        AqlQuery aqlQuery = aqlService.parseAql(result);
        ObjectMapper objectMapper = AqlDtoSerializer.getObjectMapper();
        return ResponseEntity.ok(objectMapper.writeValueAsString(aqlQuery));
    }
}
