package com.m2a.db.query;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GroupBy implements Iterable<GroupBy.GroupByProperty>, Serializable {

    private final List<GroupByProperty> properties;

    public GroupBy(GroupByProperty... properties) {
        this(Arrays.asList(properties));
    }

    public GroupBy(List<GroupByProperty> props) {
        if (null == props || props.isEmpty())
            throw new IllegalArgumentException("You have to provide at least one group by property!");
        this.properties = props;
    }

    @Override
    public Iterator<GroupByProperty> iterator() {
        return this.properties.iterator();
    }

    @Getter
    @Setter
    public static class GroupByProperty implements Serializable {
        private String propertyName;

        public GroupByProperty(String propertyName) {
            this.propertyName = propertyName;
        }
    }
}
