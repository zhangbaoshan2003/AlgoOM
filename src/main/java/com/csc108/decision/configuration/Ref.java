package com.csc108.decision.configuration;

/**
 * Created by LEGEN on 2016/5/2.
 */
public class Ref<T> {
    private T value;

    public Ref(T value){
        this.value=value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return value.equals(obj);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
