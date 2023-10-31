package io.metersphere.cdc.strategy.base;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.model.TestTrainLesson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLessonCdcOperator implements IOperator<SinkUpgradeElement<TestTrainLesson>> {

    public static final String tableName = "train_lesson";

    @Override
    public boolean support(SinkUpgradeElement<TestTrainLesson> sinkElement) {
        var source = sinkElement.getSource();
        var operatorType = sinkElement.getOperatorType();
        if (null == source || null == operatorType) {
            return false;
        }
        if (!source.getTable().equals(tableName)) {
            return false;
        }

        return true;
    }

    protected boolean validateLesson(TestTrainLesson testTrainLesson) {
        if (null == testTrainLesson) {
            return false;
        }
        var lessonId = testTrainLesson.getLessonId();
        if (null == lessonId) {
            return false;
        }

        return true;
    }

}
