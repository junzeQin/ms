package io.metersphere.cdc.strategy.user.operator;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.OperatorTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.base.AbstractUserCdcOperator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component()
@Slf4j
public class UserReadCdcOperator extends AbstractUserCdcOperator {

    @Override
    public boolean support(SinkUpgradeElement<TestTrainUser> sinkElement) {
        if (super.support(sinkElement)) {
            return sinkElement.getOperatorType().equals(OperatorTypeEnum.r);
        }

        return false;
    }

    @Override
    public void handle(SinkUpgradeElement<TestTrainUser> sinkElement) {
        var testTrainUser = sinkElement.getAfter();
        if (!super.validateUser(testTrainUser)) {
            log.warn("UserReadCdcOperator#handle validate user do not meet requirements: {}", testTrainUser);
            return;
        }
        super.handle(testTrainUser);
    }

}
