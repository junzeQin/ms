package io.metersphere.cdc.strategy.base;

import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.OperatorTypeEnum;
import io.metersphere.cdc.domain.enums.TestTrainUserTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainLessonUser;
import io.metersphere.cdc.domain.model.TestTrainProject;
import io.metersphere.outter.ITestTrainProjectService;

import java.io.Serializable;
import java.util.List;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

@Slf4j
public abstract class AbstractLessonUserCdcOperator extends AbstractProjectMemberOperator implements IOperator<SinkUpgradeElement<TestTrainLessonUser>>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String tableName = "train_lesson_user";

    @Autowired
    protected ProjectMapper projectMapper;

    @Autowired
    protected ITestTrainProjectService testTrainProjectService;

    @Override
    public boolean support(SinkUpgradeElement<TestTrainLessonUser> sinkElement) {
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

    protected boolean validateLessonUser(TestTrainLessonUser TestTrainLessonUser) {
        if (null == TestTrainLessonUser) {
            return false;
        }
        var lessonId = TestTrainLessonUser.getLessonId();
        if (null == lessonId) {
            return false;
        }
        var userName = TestTrainLessonUser.getUserName();
        if (StrUtil.isBlank(userName)) {
            return false;
        }
        var role = TestTrainLessonUser.getRole();
        if (null == role) {
            return false;
        }

        return true;
    }

    @SneakyThrows
    protected void syncProjectMember(OperatorTypeEnum operatorType, TestTrainLessonUser testTrainLessonUser) {
        log.info("TestTrainLessonUser: {}", super.objectMapper.writeValueAsString(testTrainLessonUser));

        var lessonId = testTrainLessonUser.getLessonId();
        var projectResponse = testTrainProjectService.queryParentProjectListByLessonId(lessonId);
        List<TestTrainProject> projectList;
        if (null == projectResponse) {
            log.error("Get project error");
            return;
        }
        if (CollectionUtil.isEmpty((projectList = projectResponse.getData()))) {
            log.warn("Project list return null");
            return;
        }
        TestTrainUserTypeEnum userType = TestTrainUserTypeEnum.getByCode(String.valueOf(testTrainLessonUser.getRole()));
        if (null == userType) {
            return;
        }
        projectList.forEach(x -> {
            switch (operatorType) {
                case c:
                case r:
                case u:
                    super.syncProjectMember(userType, String.valueOf(x.getProjectId()), testTrainLessonUser.getUserName());
                    return;
                case d:
                    super.userService.deleteProjectMember(String.valueOf(x.getProjectId()), testTrainLessonUser.getUserName());
                    return;
                default:
                    break;
            }
        });
    }

}
