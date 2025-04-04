package com.m2a.spring.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

@Getter
@Setter
@MappedSuperclass
public abstract class PO extends BasePO {

    @Column(name = "delete_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedDate;

    @CreatedDate
    @Column(name = "create_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @LastModifiedDate
    @Column(name = "update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    @Column(name = "version", columnDefinition = " integer DEFAULT 0 ")
    @Version
    private int version;
}
