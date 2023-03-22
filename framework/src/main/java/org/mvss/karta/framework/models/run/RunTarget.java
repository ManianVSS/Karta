package org.mvss.karta.framework.models.run;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunTarget implements Serializable {
    private static final long serialVersionUID = 1L;

    private String featureFile;

    private String javaTest;
    private String javaTestJarFile;

    private ArrayList<String> runTags;
}
