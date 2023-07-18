package org.mvss.karta.server.fitering;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.mvss.karta.server.Constants;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SearchCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    @ApiModelProperty(value = "The field to apply an operation on. If operation is binary the value property is also used.")
    protected String key = Constants.PV_ID;

    @ApiModelProperty(value = "The operator to apply on key and value. Possible values list are \"exists\", \"doesNotExists\", \"!\", \"!=\", \"=\", \"==\", \"~=\", \"like\", \"!~=\", \"notLike\", \"<=\", \"<\" , \">=\" and \">\".")
    @Builder.Default
    protected String operation = Constants.OP_EQ;

    @ApiModelProperty(value = "The value field for RHS of a binary operation.")
    protected Serializable value;

}
