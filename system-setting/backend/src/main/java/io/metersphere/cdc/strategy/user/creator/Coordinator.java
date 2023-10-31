package io.metersphere.cdc.strategy.user.creator;

import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.user.base.AbstractOperateCoordinator;
import io.metersphere.cdc.strategy.user.base.AbstractUserCreator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("Cdc:TestTrain:User:Create:Coordinator")
public class Coordinator extends AbstractOperateCoordinator {

    @Autowired
    public Coordinator(List<AbstractUserCreator> userCreatorList) {
        super(userCreatorList);
    }

    @Override
    protected String errorMessage(TestTrainUser testTrainUser) {
        return "UserCreateCoordinator#handle[" + testTrainUser + "] get user creator fault";
    }

}
