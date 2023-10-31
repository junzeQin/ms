package io.metersphere.cdc.strategy.project.operator;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.OperatorTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainProject;
import io.metersphere.cdc.strategy.base.AbstractProjectCdcOperator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component()
@Slf4j
public class ProjectDeleteCdcOperator extends AbstractProjectCdcOperator {

    @Override
    public boolean support(SinkUpgradeElement<TestTrainProject> sinkElement) {
        if (super.support(sinkElement)) {
            return sinkElement.getOperatorType().equals(OperatorTypeEnum.d);
        }

        return false;
    }

    @Override
    public void handle(SinkUpgradeElement<TestTrainProject> sinkElement) {
        var testTrainProject = sinkElement.getBefore();
        if (!super.validateProject(testTrainProject)) {
            log.warn("ProjectDeleteCdcOperator#handle validate project do not meet requirements: {}", testTrainProject);
            return;
        }
        super.systemProjectService.deleteProject(String.valueOf(testTrainProject.getProjectId()));
    }

}
