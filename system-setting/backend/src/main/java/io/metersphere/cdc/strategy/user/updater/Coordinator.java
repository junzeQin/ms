package io.metersphere.cdc.strategy.user.updater;

import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.user.base.AbstractOperateCoordinator;
import io.metersphere.cdc.strategy.user.base.AbstractUserUpdater;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("Cdc:TestTrain:User:Update:Coordinator")
public class Coordinator extends AbstractOperateCoordinator {

    @Autowired
    public Coordinator(List<AbstractUserUpdater> userUpdaterList) {
        super(userUpdaterList);
    }

    @Override
    protected String errorMessage(TestTrainUser testTrainUser) {
        return "UserUpdateCoordinator#handle[" + testTrainUser + "] get user updater fault";
    }

}
