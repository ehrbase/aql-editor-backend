package org.ehrbase.aqleditor.service;

import lombok.AllArgsConstructor;
import org.ehrbase.aqleditor.dto.containment.ResolveInput;
import org.ehrbase.aqleditor.dto.containment.ResolveOutput;
import org.ehrbase.aqleditor.resolver.Resolver;
import org.ehrbase.openehr.sdk.aql.webtemplatepath.AqlPath;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Stefan Spiska
 */
@AllArgsConstructor
@Service
public class FromService {

    private final TemplateService templateService;

    public ResolveOutput resolve(ResolveInput input) {

        WebTemplate rawWebTemplate = templateService.getRawWebTemplate(input.getTemplateId());

        AqlPath parse = AqlPath.parse(input.getAqlPath());
        ResolveOutput resolveOutput = Resolver.getOutput(parse, rawWebTemplate, input.getFrom());

        return resolveOutput;
    }
}
