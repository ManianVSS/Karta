package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.models.result.RunResult;
import org.mvss.karta.framework.utils.ParserUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class RunCompleteEvent extends Event {
    private static final long serialVersionUID = 1L;

    public RunCompleteEvent(Event event) {
        super(event);
        parameters.put(Constants.RESULT, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.RESULT), RunResult.class));
    }

    public RunCompleteEvent(String runName, RunResult result) {
        super(StandardEventsTypes.RUN_COMPLETE_EVENT, runName);
        this.parameters.put(Constants.RESULT, result);
    }

    @JsonIgnore
    public RunResult getResult() {
        return (RunResult) parameters.get(Constants.RESULT);
    }
}
