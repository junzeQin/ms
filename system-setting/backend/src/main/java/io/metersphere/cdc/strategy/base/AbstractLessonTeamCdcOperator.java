package io.metersphere.cdc.strategy.base;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.model.TestTrainLessonTeam;
import io.metersphere.commons.constants.UserConstants;
import io.metersphere.outter.ITestTrainProjectService;

import java.io.Serializable;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractLessonTeamCdcOperator extends AbstractProjectOperator implements IOperator<SinkUpgradeElement<TestTrainLessonTeam>>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String tableName = "train_team";

    @Autowired
    private ITestTrainProjectService testTrainProjectService;

    @Override
    public boolean support(SinkUpgradeElement<TestTrainLessonTeam> sinkElement) {
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

    protected boolean validateLessonTeam(TestTrainLessonTeam testTrainLessonTeam) {
        if (null == testTrainLessonTeam) {
            return false;
        }
        var lessonId = testTrainLessonTeam.getLessonId();
        if (null == lessonId) {
            return false;
        }
        var teamId = testTrainLessonTeam.getTeamId();
        if (null == teamId) {
            return false;
        }
        var projectId = testTrainLessonTeam.getProjectId();
        if (null == projectId) {
            return false;
        }

        return true;
    }

    @SneakyThrows
    protected void handle(TestTrainLessonTeam testTrainLessonTeam) {
        log.info("testTrainLessonTeam: {}", super.objectMapper.writeValueAsString(testTrainLessonTeam));
    }

    protected String acquireProjectId(TestTrainLessonTeam lessonTeam) {
        var testTrainTeamId = lessonTeam.getTeamId();
        var testTrainProjectId = lessonTeam.getProjectId();

        return testTrainTeamId + "-" + testTrainProjectId;
    }

    @SneakyThrows
    protected void syncProject(TestTrainLessonTeam testTrainLessonTeam) {
        log.info("testTrainLessonTeam: {}", super.objectMapper.writeValueAsString(testTrainLessonTeam));

        var testTrainProjectId = testTrainLessonTeam.getProjectId();
        var testTrainProject = testTrainProjectService.getProjectById(testTrainProjectId);
        if (null == testTrainProject) {
            log.error("Get project fail");
            return;
        }
        var lessonId = testTrainLessonTeam.getLessonId();
        var projectId = acquireProjectId(testTrainLessonTeam);
        var projectName = testTrainLessonTeam.getTeamName() + "-" + testTrainProject.getProjectName();
        var projectDescription = testTrainProject.getProjectDescription();
        var project = super.createProject(lessonId, projectId, projectName, projectDescription, testTrainLessonTeam.getCreateBy());
        if (null == project) {
            log.error("Create project fail");
            return;
        }
        super.syncProjectMember(projectId, lessonId, UserConstants.TEST_TRAIN_TEACHER_ROLE);
    }

}
