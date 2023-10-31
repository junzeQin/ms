package io.metersphere.cdc.strategy.lesson.team.user.operator;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.OperatorTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainLessonTeamUser;
import io.metersphere.cdc.strategy.base.AbstractLessonTeamUserCdcOperator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component()
@Slf4j
public class LessonTeamUserReadCdcOperator extends AbstractLessonTeamUserCdcOperator {

    @Override
    public boolean support(SinkUpgradeElement<TestTrainLessonTeamUser> sinkElement) {
        if (super.support(sinkElement)) {
            return sinkElement.getOperatorType().equals(OperatorTypeEnum.r);
        }

        return false;
    }

    @Override
    public void handle(SinkUpgradeElement<TestTrainLessonTeamUser> sinkElement) {
        var lessonTeamUser = sinkElement.getAfter();
        if (!super.validateLessonTeamUser(lessonTeamUser)) {
            log.warn("LessonTeamUserReadCdcOperator#handle validate lesson team user do not meet requirements: {}", lessonTeamUser);
            return;
        }
        super.syncProjectMember(lessonTeamUser);
    }

}
