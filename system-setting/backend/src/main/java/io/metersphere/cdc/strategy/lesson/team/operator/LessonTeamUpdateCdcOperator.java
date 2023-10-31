package io.metersphere.cdc.strategy.lesson.team.operator;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.OperatorTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainLessonTeam;
import io.metersphere.cdc.strategy.base.AbstractLessonTeamCdcOperator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component()
@Slf4j
public class LessonTeamUpdateCdcOperator extends AbstractLessonTeamCdcOperator {

    @Override
    public boolean support(SinkUpgradeElement<TestTrainLessonTeam> sinkElement) {
        if (super.support(sinkElement)) {
            return sinkElement.getOperatorType().equals(OperatorTypeEnum.u);
        }

        return false;
    }

    @Override
    public void handle(SinkUpgradeElement<TestTrainLessonTeam> sinkElement) {
        var testTrainLessonTeam = sinkElement.getAfter();
        if (!super.validateLessonTeam(testTrainLessonTeam)) {
            log.warn("LessonTeamUpdateCdcOperator#handle validate lesson team do not meet requirements: {}", testTrainLessonTeam);
            return;
        }
        super.syncProject(testTrainLessonTeam);
    }

}
