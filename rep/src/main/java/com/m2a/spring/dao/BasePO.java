package com.m2a.spring.dao;


import com.m2a.db.EntityModel;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BasePO implements EntityModel<Long> {

    @Id
    @GeneratedValue
    private Long id;

    @Transient
    public boolean isNew() {
        return getId() == null;
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
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BasePO other = (BasePO) obj;
        if (id == null)
            return other.id == null;
        else return id.equals(other.id);
    }
}
