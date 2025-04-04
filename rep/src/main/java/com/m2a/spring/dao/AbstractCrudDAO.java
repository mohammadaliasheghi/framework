package com.m2a.spring.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Date;

@NoRepositoryBean
public interface AbstractCrudDAO<T extends PO> extends AbstractDAO<T> {

    //implements Custom behaviour
    @Modifying
    @Query("update #{#entityName} e set e.deletedDate = ?2, e.version= (e.version+1) where e.id= ?1")
    void deleteSoft(Serializable id, Date date);
}
