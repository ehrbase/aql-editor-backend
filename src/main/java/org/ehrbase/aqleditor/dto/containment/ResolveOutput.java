package org.ehrbase.aqleditor.dto.containment;

import lombok.Data;
import org.ehrbase.openehr.sdk.aql.dto.containment.Containment;
import org.ehrbase.openehr.sdk.aql.dto.operand.IdentifiedPath;

/**
 * @author Stefan Spiska
 */
@Data
public class ResolveOutput {

    private Containment from;

    private IdentifiedPath columnExpression;
}
