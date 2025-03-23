package com.m2a.db.query;

import com.m2a.enums.Direction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Sort implements Iterable<Sort.Order>, Serializable {

    public static final Direction DEFAULT_DIRECTION = Direction.ASC;

    public Sort(Order... orders) {
        this(Arrays.asList(orders));
    }

    public Sort(List<Order> orders) {

        if (null == orders || orders.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one sort property to sort by!");
        }

        this.orders = orders;
    }

    public Sort(String... properties) {
        this(DEFAULT_DIRECTION, properties);
    }

    public Sort(Direction direction, String... properties) {
        this(direction, properties == null ? new ArrayList<String>() : Arrays.asList(properties));
    }

    public Sort(Direction direction, List<String> properties) {

        if (properties == null || properties.isEmpty())
            throw new IllegalArgumentException("You have to provide at least one property to sort by!");

        this.orders = new ArrayList<>(properties.size());
        properties.parallelStream()
                .forEach(property ->
                        this.orders.add(new Order(direction, property))
                );
    }


    private final List<Order> orders;

    @Override
    public Iterator<Order> iterator() {
        return this.orders.iterator();
    }

    @Getter
    @RequiredArgsConstructor
    public static class Order implements Serializable {
        private Direction direction;
        private String property;

        public Order(Direction direction, String property) {
            this.direction = direction;
            this.property = property;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((direction == null) ? 0 : direction.hashCode());
            result = prime * result + ((property == null) ? 0 : property.hashCode());
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
            Order other = (Order) obj;
            if (direction != other.direction)
                return false;
            if (property == null)
                return other.property == null;
            else return property.equals(other.property);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orders == null) ? 0 : orders.hashCode());
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
        Sort other = (Sort) obj;
        if (orders == null)
            return other.orders == null;
        else return orders.equals(other.orders);
    }
}
