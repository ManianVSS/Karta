package org.mvss.karta.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.*;
import org.mvss.karta.framework.models.catalog.TestType;

import javax.persistence.*;
import java.time.Duration;
import java.util.ArrayList;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(indexes = {@Index(columnList = "name"), @Index(columnList = "parent"), @Index(columnList = "parent, name")}, uniqueConstraints = {@UniqueConstraint(columnNames = {"parent", "name"})})
public class Test extends BaseModel {
    private static final long serialVersionUID = 1L;

    @JoinColumn(name = "parent")
    @JsonIncludeProperties({"id", "name", "parent"})
    @ManyToOne(fetch = FetchType.EAGER)
    private TestCategory parent;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TestType testType = TestType.FEATURE;

    private String description;

    @Builder.Default
    private Integer priority = Integer.MAX_VALUE;

    @Lob
    @Builder.Default
    private ArrayList<String> tags = new ArrayList<>();

    private String sourceArchive;

    @Lob
    @Builder.Default
    private ArrayList<String> featureSourceParsers = new ArrayList<>();

    @Lob
    @Builder.Default
    private ArrayList<String> stepRunners = new ArrayList<>();

    @Lob
    @Builder.Default
    private ArrayList<String> testDataSources = new ArrayList<>();

    private String featureFileName;

    private String javaTestClass;

    @Builder.Default
    private Boolean runAllScenarioParallely = false;

    @Builder.Default
    private Boolean chanceBasedScenarioExecution = false;

    @Builder.Default
    private Boolean exclusiveScenarioPerIteration = false;

    @JsonFormat(shape = Shape.STRING)
    private Duration runDuration;

    @JsonFormat(shape = Shape.STRING)
    private Duration coolDownBetweenIterations;

    @Builder.Default
    private long iterationsPerCoolDownPeriod = 1;

    private String threadGroup;

    @Builder.Default
    private long numberOfIterations = 1;

    @Builder.Default
    private int numberOfThreads = 1;

}
