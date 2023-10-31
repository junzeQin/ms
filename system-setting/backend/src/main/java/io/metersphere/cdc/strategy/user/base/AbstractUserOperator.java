package io.metersphere.cdc.strategy.user.base;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.metersphere.base.domain.GroupExample;
import io.metersphere.base.domain.ProjectExample;
import io.metersphere.base.domain.Workspace;
import io.metersphere.base.domain.WorkspaceExample;
import io.metersphere.base.mapper.GroupMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.WorkspaceMapper;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.base.IOperator;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.request.member.UserRequest;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import cn.hutool.core.collection.CollectionUtil;

@Slf4j
public abstract class AbstractUserOperator implements IOperator<TestTrainUser>, Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private WorkspaceMapper workspaceMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private ProjectMapper projectMapper;

    protected UserRequest testTrainUser2UserRequest(TestTrainUser testTrainUser, String... userGroupIds) {
        var userRequest = new UserRequest();
        userRequest.setId(testTrainUser.getUserName());
        userRequest.setEmail(testTrainUser.getEmail());
        userRequest.setName(testTrainUser.getNickName());
        userRequest.setPhone(testTrainUser.getPhoneNumber());
        userRequest.setPassword(testTrainUser.getPassword());
        userRequest.setPasswordEncode(false);

        return userRequest;
    }

    protected List<Map<String, Object>> mapperUserGroups(String... userGroupIds) {
        var userGroups = Lists.<Map<String, Object>>newArrayList();

        var queryWorkspace = new WorkspaceExample();
        queryWorkspace.createCriteria().andIdEqualTo("-2");
        var workspaceList = workspaceMapper.selectByExample(queryWorkspace);

        var queryProject = new ProjectExample();
        queryProject.createCriteria().andIdEqualTo("-3");
        var projectList = projectMapper.selectByExample(queryProject);

        var queryGroup = new GroupExample();
        queryGroup.createCriteria()
            .andIdIn(Lists.newArrayList(userGroupIds));
        var groupList = groupMapper.selectByExample(queryGroup);
        groupList.forEach(x -> {
            final Map<String, Object> groupMap = Maps.newHashMap();
            var type = x.getType();
            groupMap.put("type", x.getId() + "+" + type);
            List<String> idList = null;
            switch (type) {
                case UserGroupType.WORKSPACE:
                    idList = Lists.newArrayListWithExpectedSize(workspaceList.size());
                    for (Workspace workspace : workspaceList) {
                        idList.add(workspace.getId());
                    }
                    break;
                case UserGroupType.PROJECT:
                    idList = Lists.newArrayListWithExpectedSize(projectList.size());
                    for (Workspace workspace : workspaceList) {
                        idList.add(workspace.getId());
                    }
                    break;
            }
            if (CollectionUtil.isNotEmpty(idList)) {
                groupMap.put("ids", idList);
                userGroups.add(groupMap);
            }
        });

        return userGroups;
    }

}
