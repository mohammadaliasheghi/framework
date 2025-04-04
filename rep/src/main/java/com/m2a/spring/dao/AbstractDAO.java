package com.m2a.spring.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractDAO<T> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
}
