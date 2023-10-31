package io.metersphere.commons.constants;

public interface UserConstants {

    String[] TEACHER_USER_GROUP_IDS = {"ws_admin", "project_admin"};

    String[] SYSTEM_USER_GROUP_IDS = {"admin", "ws_admin", "project_admin"};

    String[] STUDENT_USER_GROUP_IDS = {"ws_member", "project_member"};

    String ADMIN = "admin";

    long TEST_TRAIN_TEACHER_ROLE = 3L;

    long TEST_TRAIN_STUDENT_ROLE = 5L;

}
