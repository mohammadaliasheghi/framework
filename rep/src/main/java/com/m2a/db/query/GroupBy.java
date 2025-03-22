package com.m2a.db.query;

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

    public record GroupByProperty(String propertyName) implements Serializable {
        //iterate record
    }
}
