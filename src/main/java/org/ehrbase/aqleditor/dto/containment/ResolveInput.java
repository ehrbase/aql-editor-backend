package org.ehrbase.aqleditor.dto.containment;

import lombok.Data;
import org.ehrbase.openehr.sdk.aql.dto.containment.Containment;

/**
 * @author Stefan Spiska
 */
@Data
public class ResolveInput {

    private String aql;
    private String templateId;
    private Containment from;
}
