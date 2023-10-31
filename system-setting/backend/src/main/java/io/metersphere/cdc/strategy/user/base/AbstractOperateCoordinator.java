package io.metersphere.cdc.strategy.user.base;

import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.commons.exception.MSException;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;

import lombok.SneakyThrows;

public abstract class AbstractOperateCoordinator implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final List<? extends AbstractUserOperator> list;

    public AbstractOperateCoordinator(List<? extends AbstractUserOperator> list) {
        this.list = list;
    }

    @SneakyThrows
    public void handle(TestTrainUser testTrainUser) {
        var userCreator = list.stream()
            .filter(y -> y.support(testTrainUser))
            .findAny()
            .orElseThrow((Supplier<Exception>) () -> new MSException(errorMessage(testTrainUser)));
        userCreator.handle(testTrainUser);
    }

    protected abstract String errorMessage(TestTrainUser testTrainUser);

}
