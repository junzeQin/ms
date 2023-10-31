package io.metersphere.cdc.strategy.base;

public interface IOperator<T> {

    boolean support(T t);

    void handle(T t);

}
