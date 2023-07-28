/*
 * Copyright (c) 2022 vitasystems GmbH and Hannover Medical School.
 *
 * This file is part of project openEHR_SDK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ehrbase.aqleditor.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.ehrbase.openehr.sdk.aql.webtemplatepath.AqlPath;
import org.ehrbase.openehr.sdk.aql.webtemplatepath.predicate.PredicateHelper;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplate;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplateInput;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplateNode;

@UtilityClass
public class MatcherUtil {

    public List<WebTemplateNode> find(AqlPath path, WebTemplate root) {

        return root.getTree().getChildren().stream()
                .map(c -> find(path, c))
                .filter(e -> !e.isEmpty())
                .findAny()
                .orElse(Collections.emptyList());
    }

    public List<WebTemplateNode> find(AqlPath path, WebTemplateNode root) {

        if (matches(path.getBaseNode(), root)) {

            if (root.getChildren().isEmpty()) {
                if (path.getNodes().size() == 1) {
                    return new ArrayList<>(Collections.singletonList(root));
                } else if (path.getNodes().size() == 2
                        && root.getInputs().stream().anyMatch(i -> MatcherUtil.matchesInput(path.getLastNode(), i))) {
                    return new ArrayList<>(Collections.singletonList(root));
                } else {
                    return Collections.emptyList();
                }
            } else {
                if (path.getNodes().size() == 1
                        || (path.getNodes().size() == 2
                                && root.getInputs().stream()
                                        .anyMatch(i -> MatcherUtil.matchesInput(path.getLastNode(), i)))) {
                    return new ArrayList<>(Collections.singletonList(root));
                } else {
                    return root.getChildren().stream()
                            .map(c -> find(path.removeStart(1), c))
                            .filter(e -> !e.isEmpty())
                            .findAny()
                            .map(l -> {
                                l.add(0, root);
                                return l;
                            })
                            .orElse(Collections.emptyList());
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * Check if {@link WebTemplateInput#getSuffix()} matches {@link AqlPath.AqlNode#getName()}. 'value' might be left out as suffix
     *
     * @param node
     * @param input
     * @return
     */
    boolean matchesInput(AqlPath.AqlNode node, WebTemplateInput input) {
        // value might be left out as suffix
        return node.getName().equals(Optional.ofNullable(input.getSuffix()).orElse("value"));
    }

    /**
     * Check whether {@link AqlPath.AqlNode} matches {@link WebTemplateNode}. that is
     * <ul>
     *   <li>path name matches</li>
     *   <li>atCode matches or is not set in {@link AqlPath.AqlNode} </li>
     *   <li>name/value matches or is not set in {@link AqlPath.AqlNode} </li>
     * </ul>
     *
     * @param path
     * @param node
     * @return
     */
    boolean matches(AqlPath.AqlNode path, WebTemplateNode node) {

        AqlPath.AqlNode nodeAqlNode = node.getAqlPathDto().getLastNode();

        if (!Objects.equals(path.getName(), nodeAqlNode.getName())) {

            return false;
        }

        String atCode = path.getAtCode();

        if (atCode != null && !atCode.equals(nodeAqlNode.getAtCode())) {
            return false;
        }

        return path.findOtherPredicate(PredicateHelper.NAME_VALUE) == null
                || path.findOtherPredicate(PredicateHelper.NAME_VALUE).equals(node.getName());
    }
}
