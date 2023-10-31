package io.metersphere.cdc.strategy.lesson.user.operator;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.OperatorTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainLessonUser;
import io.metersphere.cdc.strategy.base.AbstractLessonUserCdcOperator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component()
@Slf4j
public class LessonUserDeleteCdcOperator extends AbstractLessonUserCdcOperator {

    @Override
    public boolean support(SinkUpgradeElement<TestTrainLessonUser> sinkElement) {
        if (super.support(sinkElement)) {
            return sinkElement.getOperatorType().equals(OperatorTypeEnum.d);
        }

        return false;
    }

    @Override
    public void handle(SinkUpgradeElement<TestTrainLessonUser> sinkElement) {
        var lessonUser = sinkElement.getBefore();
        if (!super.validateLessonUser(lessonUser)) {
            log.warn("LessonUserDeleteCdcOperator#handle validate lesson user do not meet requirements: {}", lessonUser);
            return;
        }
        super.syncProjectMember(OperatorTypeEnum.d, lessonUser);
    }

}
