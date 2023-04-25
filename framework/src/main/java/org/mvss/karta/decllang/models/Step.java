package org.mvss.karta.decllang.models;

import lombok.*;

import java.io.Serializable;
import java.util.HashMap;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Step implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private HashMap<String, Serializable> data;
    private String node;
}
