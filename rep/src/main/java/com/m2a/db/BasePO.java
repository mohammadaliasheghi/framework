package com.m2a.db;

import jakarta.persistence.*;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class BasePO implements EntityModel<Long> {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_generator")
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BasePO other = (BasePO) obj;
        if (id == null)
            return other.id == null;
        else
            return id.equals(other.id);
    }
}
