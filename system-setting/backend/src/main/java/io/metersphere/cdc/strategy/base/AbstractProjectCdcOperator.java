package io.metersphere.cdc.strategy.base;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.model.TestTrainProject;

import java.io.Serializable;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import cn.hutool.core.util.StrUtil;

@Slf4j
public abstract class AbstractProjectCdcOperator extends AbstractProjectOperator implements IOperator<SinkUpgradeElement<TestTrainProject>>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String tableName = "train_project";

    @Override
    public boolean support(SinkUpgradeElement<TestTrainProject> sinkElement) {
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

    protected boolean validateProject(TestTrainProject testTrainProject) {
        if (null == testTrainProject) {
            return false;
        }
        var lessonId = testTrainProject.getLessonId();
        if (null == lessonId) {
            return false;
        }
        var parentId = testTrainProject.getParentId();
        if (null != parentId) {
            return false;
        }
        var status = testTrainProject.getStatus();
        if (StrUtil.isBlank(status)) {
            log.error("Project status null");
            return false;
        }

        return true;
    }

    @SneakyThrows
    protected void createProject(TestTrainProject testTrainProject) {
        log.info("testTrainProject: {}", objectMapper.writeValueAsString(testTrainProject));

        var status = testTrainProject.getStatus();
        if (!status.equals("1")) {
            log.warn("Project status out of bounds");
            return;
        }
        var lessonId = testTrainProject.getLessonId();
        var projectId = testTrainProject.getProjectId();
        var projectName = testTrainProject.getProjectName();
        var projectDescription = testTrainProject.getProjectDescription();
        var project = super.createProject(lessonId, String.valueOf(projectId), projectName, projectDescription, testTrainProject.getCreateBy());
        if (null == project) {
            log.error("Create project fail");
            return;
        }
        super.syncProjectMember(String.valueOf(projectId), lessonId, null);
    }

}
