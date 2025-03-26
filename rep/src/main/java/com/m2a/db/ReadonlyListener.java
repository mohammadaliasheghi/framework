package com.m2a.db;

import org.hibernate.event.spi.*;

public class ReadonlyListener implements PreInsertEventListener, PreUpdateEventListener, PreDeleteEventListener {

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Class<?> entityClass = event.getEntity().getClass();
        return entityClass.isAnnotationPresent(Readonly.class);
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        Class<?> entityClass = event.getEntity().getClass();
        return entityClass.isAnnotationPresent(Readonly.class);
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        Class<?> entityClass = event.getEntity().getClass();
        return entityClass.isAnnotationPresent(Readonly.class);
    }
}
