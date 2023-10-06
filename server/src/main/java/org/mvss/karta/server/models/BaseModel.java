package org.mvss.karta.server.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
//@JsonIgnoreProperties( value = {"createdAt", "updatedAt", "version"})//, allowGetters = true )
public abstract class BaseModel implements Serializable {
    @Id
    @GeneratedValue
    protected Long id;

    @Version
    protected Integer version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    protected Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    protected Date updatedAt;

    public void copyIdAndVersionFrom(BaseModel baseModel) {
        this.id = baseModel.id;
        this.version = baseModel.version;
    }
}
