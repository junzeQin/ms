package io.metersphere.cdc.strategy.base;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ProjectExample;
import io.metersphere.cdc.domain.model.TestTrainLesson;
import io.metersphere.commons.constants.UserConstants;
import io.metersphere.request.AddProjectRequest;

import java.util.List;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.MutablePair;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

@Slf4j
public abstract class AbstractProjectOperator extends AbstractProjectMemberOperator {

    @SneakyThrows
    protected Project createProject(Long lessonId, String projectId, String projectName, String projectDescription, String createUser) {
        var queryProject = new ProjectExample();
        queryProject.createCriteria().andIdEqualTo(String.valueOf(projectId));
        List<Project> projects = super.projectMapper.selectByExample(queryProject);
        if (CollUtil.isNotEmpty(projects)) {
            log.warn("Project already exist");
            Project project = projects.get(0);
            updateProject(project, projectName, projectDescription);

            return null;
        }
        var projectRequest = new AddProjectRequest();
        projectRequest
            .setProtocal("http")
            .setCaseTemplateId("b395d8fe-2ad6-4de7-81d3-2006b53a97c8")
            .setIssueTemplateId("5d7c87d2-f405-4ec1-9a3d-71b514cdfda3")
            .setApiTemplateId("1bb1fae3-5d83-45a1-80ab-31135d91de39")
            .setWorkspaceId("-2")
            .setCreateUser(StrUtil.isBlank(createUser) ? UserConstants.ADMIN : createUser)
            .setId(String.valueOf(projectId))
            .setName(projectName)
            .setDescription(projectDescription);

        return super.systemProjectService.createProject(projectRequest);
    }

    protected String getProjectName(Long lessonId, String projectName) {
        var lessonResponse = super.testTrainLessonService.getLessonById(lessonId);
        TestTrainLesson lesson;
        if (null == lessonResponse || null == (lesson = lessonResponse.getLesson())) {
            log.error("Get lesson info error");
            return projectName;
        }
        var lessonName = lesson.getLessonName();
        var className = lesson.getClassName();
        var fullProjectName = "";
        if (StrUtil.isNotBlank(lessonName)) {
            fullProjectName += lessonName;
        }
        if (StrUtil.isBlank(fullProjectName)) {
            if (StrUtil.isBlank(className)) {
                fullProjectName = projectName;
            } else {
                fullProjectName += (className + "-" + projectName);
            }
        } else {
            if (StrUtil.isBlank(className)) {
                fullProjectName += ("-" + projectName);
            } else {
                fullProjectName += ("-" + className + "-" + projectName);
            }
        }

        return fullProjectName;
    }

    protected void updateProject(Project project, String projectName, String projectDescription) {
        AddProjectRequest updateProject = new AddProjectRequest();
        updateProject.setId(project.getId());
        var projectNewName = changeProjectName(project.getName(), projectName);
        if (projectNewName.getLeft().equals(Boolean.TRUE)) {
            updateProject.setName(projectNewName.getRight());
        }
        updateProject.setDescription(projectDescription);
        super.systemProjectService.updateProject(updateProject);
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
