package io.metersphere.cdc.strategy.project.operator;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ProjectExample;
import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.OperatorTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainProject;
import io.metersphere.cdc.strategy.base.AbstractProjectCdcOperator;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

@Component()
@Slf4j
public class ProjectUpdateCdcOperator extends AbstractProjectCdcOperator {

    @Override
    public boolean support(SinkUpgradeElement<TestTrainProject> sinkElement) {
        if (super.support(sinkElement)) {
            return sinkElement.getOperatorType().equals(OperatorTypeEnum.u);
        }

        return false;
    }

    @Override
    public void handle(SinkUpgradeElement<TestTrainProject> sinkElement) {
        var testTrainProject = sinkElement.getAfter();
        if (!super.validateProject(testTrainProject)) {
            log.warn("ProjectUpdateCdcOperator#handle validate project do not meet requirements: {}", testTrainProject);
            return;
        }
        var projectId = testTrainProject.getProjectId();
        var queryProject = new ProjectExample();
        queryProject.createCriteria().andIdEqualTo(String.valueOf(projectId));
        List<Project> projects = super.projectMapper.selectByExample(queryProject);
        if (CollectionUtil.isEmpty(projects)) {
            log.error("Project does not exist");
            super.createProject(testTrainProject);
            return;
        }
        Project project = projects.get(0);
        super.updateProject(project, testTrainProject.getProjectName(), testTrainProject.getProjectDescription());
    }

    private static MutablePair<Boolean, String> changeProjectName(String rawName, String projectName) {
        if (StrUtil.isBlank(rawName)) {
            return MutablePair.of(Boolean.TRUE, projectName);
        }
        if (rawName.equals(projectName)) {
            return MutablePair.of(Boolean.FALSE, projectName);
        }

        return MutablePair.of(Boolean.TRUE, projectName);
    }

}
