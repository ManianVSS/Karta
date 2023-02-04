package org.mvss.karta.framework.nodes;

import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.enums.NodeType;

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
public class KartaNodeConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private UUID uuid = UUID.randomUUID();

    /**
     * Name of the Karta node. This ideally should indicate the role of the node if not a minion.
     */
    @Builder.Default
    private String name = "local";

    /**
     * The host name/IP for the node.
     */
    @Builder.Default
    private String host = "localhost";

    /**
     * The TCP/IP port for the node.
     */
    @Builder.Default
    private int port = 17171;

    /**
     * Indicates if SSL is to be used for connection.
     */
    @Builder.Default
    private boolean enableSSL = true;

    /**
     * Indicates if the node is a minion. Minions are used for load sharing to run feature iterations.
     */
    @Builder.Default
    private boolean minion = false;

    /**
     * The node type RMI or REST.
     */
    @Builder.Default
    private NodeType nodeType = NodeType.RMI;

    /**
     * The custom implementation class for KartaNode.
     * This class needs to have a constructor which accepts
     */
    @Builder.Default
    private String implementation = Constants.EMPTY_STRING;
}
