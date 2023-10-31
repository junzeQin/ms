package io.metersphere.cdc.strategy.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.cdc.domain.enums.TestTrainUserTypeEnum;
import io.metersphere.commons.constants.UserGroupConstants;
import io.metersphere.outter.ITestTrainLessonService;
import io.metersphere.outter.domain.request.TestTrainLessonUserRequest;
import io.metersphere.request.member.AddMemberRequest;
import io.metersphere.service.SystemProjectService;
import io.metersphere.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

@Slf4j
public abstract class AbstractProjectMemberOperator {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ProjectMapper projectMapper;

    @Autowired
    protected ITestTrainLessonService testTrainLessonService;

    @Autowired
    protected SystemProjectService systemProjectService;

    @Autowired
    protected UserService userService;

    protected void syncProjectMember(String projectId, Long lessonId, Long role) {
        var queryLessonUserRequest = new TestTrainLessonUserRequest();
        queryLessonUserRequest.setLessonId(lessonId);
        queryLessonUserRequest.setRole(role);
        var lessonUserResponse = testTrainLessonService.queryLessonUser(queryLessonUserRequest);
        if (null == lessonUserResponse) {
            return;
        }
        var lessonUserList = lessonUserResponse.getLessonUserList();
        if (CollectionUtil.isEmpty(lessonUserList)) {
            return;
        }
        lessonUserList.forEach(x -> {
            var userTypeStr = x.getUserType();
            if (StrUtil.isBlank(userTypeStr)) {
                return;
            }
            TestTrainUserTypeEnum userType = TestTrainUserTypeEnum.getByCode(userTypeStr);
            if (null == userType) {
                return;
            }
            syncProjectMember(userType, projectId, x.getUserName());
        });
    }

    protected void syncProjectMember(TestTrainUserTypeEnum userType, String projectId, String userName) {
        AddMemberRequest projectMember;
        switch (userType) {
            case SYSTEM:
            case EDUCATIONAL_ADMINISTRATOR:
            case TEACHING_ASSISTANT:
            case TEACHER:
                projectMember = new AddMemberRequest()
                    .setUserIds(Lists.newArrayList(userName))
                    .setGroupIds(Lists.newArrayList(UserGroupConstants.PROJECT_ADMIN))
                    .setProjectId(projectId);
                userService.addProjectMember(projectMember);

                break;
            case STUDENT:
                projectMember = new AddMemberRequest()
                    .setUserIds(Lists.newArrayList(userName))
                    .setGroupIds(Lists.newArrayList(UserGroupConstants.PROJECT_MEMBER))
                    .setProjectId(projectId);
                userService.addProjectMember(projectMember);

                break;
            default:
                break;
        }
    }

    protected void syncProjectMember(String projectId, String userName, String userGroup) {
        var projectMember = new AddMemberRequest()
            .setUserIds(Lists.newArrayList(userName))
            .setGroupIds(Lists.newArrayList(userGroup))
            .setProjectId(projectId);
        userService.addProjectMember(projectMember);
    }

}
