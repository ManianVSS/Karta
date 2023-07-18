package org.mvss.karta.framework.nodes.invserver;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * This class groups the node configuration for a Karta node
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class WorkerConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private UUID uuid = UUID.randomUUID();

    @Builder.Default
    private String name = "local";

    @Builder.Default
    private String clientSecret = "changeIt";

    @Builder.Default
    private boolean minion = false;
}
