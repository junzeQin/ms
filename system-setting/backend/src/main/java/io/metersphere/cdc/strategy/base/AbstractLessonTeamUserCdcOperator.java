package io.metersphere.cdc.strategy.base;

import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.model.TestTrainLessonTeamUser;
import io.metersphere.commons.constants.UserGroupConstants;

import java.io.Serializable;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import cn.hutool.core.util.StrUtil;

@Slf4j
public abstract class AbstractLessonTeamUserCdcOperator extends AbstractProjectMemberOperator implements IOperator<SinkUpgradeElement<TestTrainLessonTeamUser>>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String tableName = "train_team_user";

    @Override
    public boolean support(SinkUpgradeElement<TestTrainLessonTeamUser> sinkElement) {
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

    protected boolean validateLessonTeamUser(TestTrainLessonTeamUser testTrainLessonTeamUser) {
        if (null == testTrainLessonTeamUser) {
            return false;
        }
        var teamId = testTrainLessonTeamUser.getTeamId();
        if (null == teamId) {
            return false;
        }
        var userName = testTrainLessonTeamUser.getUserName();
        if (StrUtil.isBlank(userName)) {
            return false;
        }

        return true;
    }

    @SneakyThrows
    protected void handle(TestTrainLessonTeamUser testTrainLessonTeamUser) {
        log.info("testTrainLessonTeamUser: {}", super.objectMapper.writeValueAsString(testTrainLessonTeamUser));
    }

    protected String acquireProjectId(TestTrainLessonTeamUser lessonTeam) {
        var testTrainTeamId = lessonTeam.getTeamId();
        var testProjectId = lessonTeam.getProjectId();

        return testTrainTeamId + "-" + testProjectId;
    }

    protected void syncProjectMember(TestTrainLessonTeamUser lessonTeamUser) {
        var projectId = acquireProjectId(lessonTeamUser);
        super.syncProjectMember(projectId, lessonTeamUser.getUserName(), UserGroupConstants.PROJECT_MEMBER);
    }

}
