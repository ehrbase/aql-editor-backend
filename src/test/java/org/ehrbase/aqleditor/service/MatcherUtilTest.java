package org.ehrbase.aqleditor.service;

import java.util.List;
import org.ehrbase.openehr.sdk.aql.webtemplatepath.AqlPath;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplate;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplateNode;
import org.junit.jupiter.api.Test;

/**
 * @author Stefan Spiska
 */
class MatcherUtilTest {

    @Test
    void find() {

        TestDataTemplateProvider testDataTemplateProvider = new TestDataTemplateProvider();

        WebTemplate webTemplate = testDataTemplateProvider
                .buildIntrospect("ehrbase_blood_pressure_simple.de.v0")
                .get();

        AqlPath path = AqlPath.parse(
                "/content[openEHR-EHR-OBSERVATION.sample_blood_pressure.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value");

        List<WebTemplateNode> nodes = MatcherUtil.find(path, webTemplate);

        System.out.println(nodes);
    }
}
