package io.metersphere.cdc.strategy.lesson.team.operator;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.OperatorTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainLessonTeam;
import io.metersphere.cdc.strategy.base.AbstractLessonTeamCdcOperator;
import io.metersphere.service.SystemProjectService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component()
@Slf4j
public class LessonTeamDeleteCdcOperator extends AbstractLessonTeamCdcOperator {

    @Autowired
    private SystemProjectService systemProjectService;

    @Override
    public boolean support(SinkUpgradeElement<TestTrainLessonTeam> sinkElement) {
        if (super.support(sinkElement)) {
            return sinkElement.getOperatorType().equals(OperatorTypeEnum.d);
        }

        return false;
    }

    @Override
    public void handle(SinkUpgradeElement<TestTrainLessonTeam> sinkElement) {
        var testTrainLessonTeam = sinkElement.getBefore();
        if (!super.validateLessonTeam(testTrainLessonTeam)) {
            log.warn("LessonTeamDeleteCdcOperator#handle validate lesson team do not meet requirements: {}", testTrainLessonTeam);
            return;
        }
        var projectId = super.acquireProjectId(testTrainLessonTeam);
        systemProjectService.deleteProject(projectId);
    }

}
