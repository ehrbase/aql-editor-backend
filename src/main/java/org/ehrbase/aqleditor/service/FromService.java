package org.ehrbase.aqleditor.service;

import java.util.List;
import java.util.Random;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.ReverseListIterator;
import org.ehrbase.aqleditor.dto.containment.ResolveInput;
import org.ehrbase.aqleditor.dto.containment.ResolveOutput;
import org.ehrbase.openehr.sdk.aql.dto.containment.Containment;
import org.ehrbase.openehr.sdk.aql.dto.containment.ContainmentClassExpression;
import org.ehrbase.openehr.sdk.aql.dto.operand.IdentifiedPath;
import org.ehrbase.openehr.sdk.aql.dto.path.AqlObjectPath;
import org.ehrbase.openehr.sdk.aql.webtemplatepath.AqlPath;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplate;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplateNode;
import org.springframework.stereotype.Service;

/**
 * @author Stefan Spiska
 */
@AllArgsConstructor
@Service
public class FromService {

    private static final List<String> resolveTo = List.of("COMPOSITION", "OBSERVATION");
    private final Random random = new Random();

    private final TemplateService templateService;

    public ResolveOutput resolve(ResolveInput input) {

        WebTemplate rawWebTemplate = templateService.getRawWebTemplate(input.getTemplateId());

        AqlPath parse = AqlPath.parse(input.getAql());
        List<WebTemplateNode> nodes = MatcherUtil.find(parse, rawWebTemplate);

        ReverseListIterator reverseListIterator = new ReverseListIterator(nodes);

        WebTemplateNode next = null;
        while (reverseListIterator.hasNext()) {

            next = (WebTemplateNode) reverseListIterator.next();

            if (resolveTo.contains(next.getRmType())) {
                break;
            }
        }

        ContainmentClassExpression fromEhr = new ContainmentClassExpression();
        fromEhr.setType("EHR");
        fromEhr.setIdentifier("e");

        ContainmentClassExpression fromLocatable = new ContainmentClassExpression();
        fromLocatable.setType(next.getRmType());
        fromLocatable.setIdentifier(next.getRmType().toLowerCase().substring(0, 1) + "_" + random.nextInt(999));

        fromEhr.setContains(fromLocatable);

        ResolveOutput resolveOutput = new ResolveOutput();

        resolveOutput.setFrom(join(input.getFrom(), fromEhr));

        IdentifiedPath identifiedPath = new IdentifiedPath();

        identifiedPath.setFrom(fromLocatable);

        AqlPath relativ = parse.removeStart(next.getAqlPathDto());

        identifiedPath.setPath(AqlObjectPath.fromAqlPath(relativ));

        resolveOutput.setColumnExpression(identifiedPath);

        return resolveOutput;
    }

    private Containment join(Containment old, Containment add) {

        return add;
    }
}
