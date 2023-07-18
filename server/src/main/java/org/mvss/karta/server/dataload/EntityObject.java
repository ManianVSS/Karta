package org.mvss.karta.server.dataload;

import lombok.*;

import java.io.Serializable;
import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class EntityObject implements Serializable {
    private String entityClass;
    private EntityAction entityAction;
    private HashMap<String, Serializable> data;
}
