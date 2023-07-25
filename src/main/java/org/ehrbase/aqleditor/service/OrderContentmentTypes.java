package org.ehrbase.aqleditor.service;

import java.util.List;

/**
 * @author Stefan Spiska
 */
public enum OrderContentmentTypes {
    CLUSTER(),
    OBSERVATION(CLUSTER),
    EVALUATION(CLUSTER),
    COMPOSITION(OBSERVATION, EVALUATION),
    EHR(COMPOSITION);

    private final List<OrderContentmentTypes> children;

    OrderContentmentTypes(OrderContentmentTypes... children) {
        this.children = List.of(children);
    }

    public static boolean isChild(OrderContentmentTypes parent, OrderContentmentTypes child) {

        if (parent.children.isEmpty()) {
            return false;
        } else {

            return parent.children.contains(child) || parent.children.stream().anyMatch(p -> isChild(p, child));
        }
    }

    public static boolean isChild(String parentType, String childType) {

        return isChild(OrderContentmentTypes.valueOf(parentType), OrderContentmentTypes.valueOf(childType));
    }
}
