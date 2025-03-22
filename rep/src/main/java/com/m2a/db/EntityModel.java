package com.m2a.db;

import java.io.Serializable;

public interface EntityModel<ID extends Serializable> extends Serializable {

    ID getId();

    void setId(ID id);
}
