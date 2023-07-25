package org.ehrbase.aqleditor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.ReverseListIterator;
import org.ehrbase.aqleditor.dto.containment.ResolveInput;
import org.ehrbase.aqleditor.dto.containment.ResolveOutput;
import org.ehrbase.openehr.sdk.aql.dto.containment.Containment;
import org.ehrbase.openehr.sdk.aql.dto.containment.ContainmentClassExpression;
import org.ehrbase.openehr.sdk.aql.dto.containment.ContainmentSetOperator;
import org.ehrbase.openehr.sdk.aql.dto.containment.ContainmentSetOperatorSymbol;
import org.ehrbase.openehr.sdk.aql.dto.operand.IdentifiedPath;
import org.ehrbase.openehr.sdk.aql.dto.operand.StringPrimitive;
import org.ehrbase.openehr.sdk.aql.dto.path.AndOperatorPredicate;
import org.ehrbase.openehr.sdk.aql.dto.path.AqlObjectPath;
import org.ehrbase.openehr.sdk.aql.dto.path.ComparisonOperatorPredicate;
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

        fromLocatable.setPredicates(new ArrayList<>());
        fromLocatable.getPredicates().add(new AndOperatorPredicate(new ArrayList<>()));
        ComparisonOperatorPredicate e = new ComparisonOperatorPredicate();
        e.setPath(AqlObjectPath.builder().node("archetype_node_id").build());
        e.setOperator(ComparisonOperatorPredicate.PredicateComparisonOperator.EQ);
        e.setValue(new StringPrimitive(next.getNodeId()));
        fromLocatable.getPredicates().get(0).getOperands().add(e);

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
        if (old == null) {
            return add;
        }

        if (old instanceof ContainmentClassExpression classExpression) {
            ContainmentClassExpression addClassExpression1 = (ContainmentClassExpression) add;
            if (matches(classExpression, addClassExpression1)) {

                addClassExpression1.setIdentifier(classExpression.getIdentifier());

                if (classExpression.getContains() != null && addClassExpression1.getContains() != null) {
                    classExpression.setContains(join(classExpression.getContains(), addClassExpression1.getContains()));
                }
                return classExpression;
            } else if (OrderContentmentTypes.isChild(classExpression.getType(), addClassExpression1.getType())) {

                if (classExpression.getContains() != null) {

                    classExpression.setContains(join(classExpression.getContains(), addClassExpression1));
                    return classExpression;

                } else {
                    classExpression.setContains(add);
                    return classExpression;
                }
            }
        }

        ContainmentSetOperator containmentSetOperator = new ContainmentSetOperator();
        containmentSetOperator.setSymbol(ContainmentSetOperatorSymbol.AND);
        containmentSetOperator.setValues(new ArrayList<>());
        containmentSetOperator.getValues().add(old);
        containmentSetOperator.getValues().add(add);

        return containmentSetOperator;
    }

    private boolean matches(ContainmentClassExpression old, ContainmentClassExpression add) {

        return old.getType().equals(add.getType()) && Objects.equals(extractAryctype(old), extractAryctype(add));
    }

    private String extractAryctype(ContainmentClassExpression classExpression) {

        if (classExpression.getPredicates() == null) {
            return null;
        }

        return classExpression.getPredicates().stream()
                .map(o -> o.getOperands())
                .flatMap(List::stream)
                .filter(p -> p.getPath().getPathNodes().get(0).getAttribute().equals("archetype_node_id"))
                .map(p -> p.getValue())
                .map(p -> StringPrimitive.class.cast(p))
                .map(StringPrimitive::getValue)
                .findAny()
                .orElse(null);
    }
}
