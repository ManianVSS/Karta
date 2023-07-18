package org.mvss.karta.server.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(indexes = {@Index(columnList = "name"), @Index(columnList = "parent"), @Index(columnList = "parent, name")}, uniqueConstraints = {@UniqueConstraint(columnNames = {"parent", "name"})})
public class TestCategory extends BaseModel {
    private static final long serialVersionUID = 1L;

    @JoinColumn(name = "parent")
    @JsonIncludeProperties({"id", "name", "parent"})
    @ManyToOne(fetch = FetchType.EAGER)
    private TestCategory parent;

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    @Lob
    private ArrayList<String> tags = new ArrayList<>();

    @Builder.Default
    @Lob
    private ArrayList<String> featureSourceParsers = new ArrayList<>();

    @Builder.Default
    @Lob
    private ArrayList<String> stepRunners = new ArrayList<>();

    @Builder.Default
    @Lob
    private ArrayList<String> testDataSources = new ArrayList<>();

    private String threadGroup;

    @Lob
    @Builder.Default
    private ArrayList<String> featureFiles = new ArrayList<>();
}
