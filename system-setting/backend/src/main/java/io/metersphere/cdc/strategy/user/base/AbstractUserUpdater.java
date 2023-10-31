package io.metersphere.cdc.strategy.user.base;

import io.metersphere.base.domain.User;
import io.metersphere.base.domain.UserGroupExample;
import io.metersphere.base.mapper.UserGroupMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.cdc.domain.enums.TestTrainDeleteFlagEnum;
import io.metersphere.cdc.domain.enums.TestTrainUserStatusEnum;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.commons.constants.UserStatus;
import io.metersphere.request.member.UserRequest;
import io.metersphere.service.UserService;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import cn.hutool.core.collection.CollectionUtil;

@Slf4j
public abstract class AbstractUserUpdater extends AbstractUserOperator {

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    protected boolean validate(TestTrainUser testTrainUser) {
        /*if (testTrainUser.getUserStatus().equals(TestTrainUserStatusEnum.DEACTIVATE)) {
            return false;
        }
        if (testTrainUser.getUserDeleteFlag().equals(TestTrainUserDeleteFlagEnum.DELETE)) {
            return false;
        }*/

        return true;
    }

    protected UserRequest testTrainUser2UserRequest(TestTrainUser testTrainUser, String... userGroupIds) {
        var userRequest = super.testTrainUser2UserRequest(testTrainUser);
        if (testTrainUser.getUserStatus().equals(TestTrainUserStatusEnum.DEACTIVATE)
            || testTrainUser.getUserDeleteFlag().equals(TestTrainDeleteFlagEnum.DELETE)) {
            userRequest.setStatus(UserStatus.DISABLED);
        }
        userRequest.setGroups(mapperUserGroups(testTrainUser.getMsUser().getId(), userGroupIds));

        return userRequest;
    }

    protected List<Map<String, Object>> mapperUserGroups(String userId, String... userGroupIds) {
        var queryUserGroup = new UserGroupExample();
        queryUserGroup.createCriteria().andUserIdEqualTo(userId);
        var userGroupList = userGroupMapper.selectByExample(queryUserGroup);
        if (CollectionUtil.isEmpty(userGroupList)) {
            return super.mapperUserGroups(userGroupIds);
        }

        return null;
    }

    protected void updateUser(TestTrainUser testTrainUser, String... userGroupIds) {
        log.info("UserUpdater userRequest: {}", testTrainUser);
        var updateUser = new User();
        updateUser.setId(testTrainUser.getUserName());
        updateUser.setName(testTrainUser.getNickName());
        updateUser.setEmail(testTrainUser.getEmail());
        updateUser.setPassword(testTrainUser.getPassword());
        updateUser.setPhone(testTrainUser.getPhoneNumber());
        userService.updateUser(updateUser);
        var userRequest = testTrainUser2UserRequest(testTrainUser, userGroupIds);
        userService.updateUserRole(userRequest);
    }

}
