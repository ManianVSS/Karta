package org.mvss.karta.samples.stepdefinitions;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SamplePropertyType implements Serializable {
    private static final long serialVersionUID = 1L;

    private String v2v1;
    private String[] v2v2;
}
