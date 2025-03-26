package com.m2a.web;

import com.m2a.enums.Operator;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QParam {

    Operator operator() default Operator.EQUAL;

    String propertyName() default "";
}
