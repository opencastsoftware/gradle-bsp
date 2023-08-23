package com.opencastsoftware.gradle.bsp;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.function.Function;

public class PropertyMatcher<A, B> extends FeatureMatcher<A, B> {
    private final Function<A, B> propGetter;
    public PropertyMatcher(String propName, Function<A, B> propGetter, Matcher<? super B> subMatcher) {
        super(subMatcher, propName, propName);
        this.propGetter = propGetter;
    }

    @Override
    protected B featureValueOf(A actual) {
        return propGetter.apply(actual);
    }

    public static <A, B> PropertyMatcher<A, B> hasProperty(String propName, Function<A, B> propGetter, Matcher<? super B> subMatcher) {
         return new PropertyMatcher<>(propName, propGetter, subMatcher);
    }
}
