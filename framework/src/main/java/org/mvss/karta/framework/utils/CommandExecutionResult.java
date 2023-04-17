package org.mvss.karta.framework.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@ToString
@AllArgsConstructor
public class CommandExecutionResult {
    protected Instant startTime;
    protected Instant endTime;
    protected final int exitCode;
    protected final byte[] output;
}
