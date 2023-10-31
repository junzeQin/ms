package io.metersphere.cdc.strategy.lesson.operator;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.OperatorTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainLesson;
import io.metersphere.cdc.strategy.base.AbstractLessonCdcOperator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component()
@Slf4j
public class LessonDeleteCdcOperator extends AbstractLessonCdcOperator {

    @Override
    public boolean support(SinkUpgradeElement<TestTrainLesson> sinkElement) {
        if (super.support(sinkElement)) {
            return sinkElement.getOperatorType().equals(OperatorTypeEnum.d);
        }

        return false;
    }

    @Override
    public void handle(SinkUpgradeElement<TestTrainLesson> sinkElement) {
        var testTrainLesson = sinkElement.getBefore();
        if (!super.validateLesson(testTrainLesson)) {
            log.warn("LessonDeleteCdcOperator#handle validate lesson do not meet requirements: {}", testTrainLesson);
            return;
        }
    }

}
