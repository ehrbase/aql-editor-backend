package org.ehrbase.aqleditor.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections.iterators.ReverseListIterator;
import org.ehrbase.aqleditor.dto.containment.ResolveOutput;
import org.ehrbase.aqleditor.service.OrderContentmentTypes;
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

/**
 * @author Stefan Spiska
 */
@UtilityClass
public class Resolver {

    private final List<String> resolveTo =
            Arrays.stream(OrderContentmentTypes.values()).map(Enum::toString).toList();
    private final Random random = new Random();

    public ResolveOutput getOutput(AqlPath parse, WebTemplate rawWebTemplate, Containment old) {
        WebTemplateNode next = Resolver.geResolvedWebTemplateNode(parse, rawWebTemplate);

        ContainmentClassExpression fromEhr = buildEhr();

        ContainmentClassExpression fromLocatable = buildContainmentClassExpression(next);

        fromEhr.setContains(fromLocatable);

        ResolveOutput resolveOutput = new ResolveOutput();

        resolveOutput.setFrom(Resolver.join(old, fromEhr));

        IdentifiedPath identifiedPath = buildIdentifiedPath(parse, fromLocatable, next);

        resolveOutput.setColumnExpression(identifiedPath);
        return resolveOutput;
    }

    private static IdentifiedPath buildIdentifiedPath(
            AqlPath parse, ContainmentClassExpression fromLocatable, WebTemplateNode next) {
        IdentifiedPath identifiedPath = new IdentifiedPath();

        identifiedPath.setRoot(fromLocatable);

        AqlPath relativ = parse.removeStart(next.getAqlPathDto());

        identifiedPath.setPath(AqlObjectPath.fromAqlPath(relativ));
        return identifiedPath;
    }

    private static ContainmentClassExpression buildEhr() {
        ContainmentClassExpression fromEhr = new ContainmentClassExpression();
        fromEhr.setType("EHR");
        fromEhr.setIdentifier("e");
        return fromEhr;
    }

    private static ContainmentClassExpression buildContainmentClassExpression(WebTemplateNode next) {
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
        return fromLocatable;
    }

    public Containment join(Containment old, Containment add) {
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

    public WebTemplateNode geResolvedWebTemplateNode(AqlPath parse, WebTemplate rawWebTemplate) {
        List<WebTemplateNode> nodes = MatcherUtil.find(parse, rawWebTemplate);

        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Not matching Nodes found");
        }

        ReverseListIterator reverseListIterator = new ReverseListIterator(nodes);

        WebTemplateNode next = null;
        while (reverseListIterator.hasNext()) {

            next = (WebTemplateNode) reverseListIterator.next();

            if (resolveTo.contains(next.getRmType())) {
                break;
            }
        }
        return next;
    }

    private boolean matches(ContainmentClassExpression old, ContainmentClassExpression add) {

        return old.getType().equals(add.getType()) && Objects.equals(extractArchetype(old), extractArchetype(add));
    }

    private String extractArchetype(ContainmentClassExpression classExpression) {

        if (classExpression.getPredicates() == null) {
            return null;
        }

        return classExpression.getPredicates().stream()
                .map(AndOperatorPredicate::getOperands)
                .flatMap(List::stream)
                .filter(p -> p.getPath().getPathNodes().get(0).getAttribute().equals("archetype_node_id"))
                .map(ComparisonOperatorPredicate::getValue)
                .map(StringPrimitive.class::cast)
                .map(StringPrimitive::getValue)
                .findAny()
                .orElse(null);
    }
}
