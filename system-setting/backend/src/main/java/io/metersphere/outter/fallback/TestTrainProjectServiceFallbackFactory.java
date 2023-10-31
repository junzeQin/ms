package io.metersphere.outter.fallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import io.metersphere.cdc.domain.model.TestTrainProject;
import io.metersphere.outter.ITestTrainProjectService;
import io.metersphere.outter.domain.R;
import io.metersphere.outter.domain.response.TestTrainProjectResponse;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestTrainProjectServiceFallbackFactory implements FallbackFactory<ITestTrainProjectService> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ITestTrainProjectService create(Throwable throwable) {
        return new ITestTrainProjectService() {

            @Override
            public TestTrainProjectResponse getProjectById(Long projectId) {
                log.error("Method: {}\nrequest:{}\ncause: {}", "ITestTrainProjectService.getProject",
                    projectId,
                    Throwables.getStackTraceAsString(throwable));

                return null;
            }

            @Override
            public R<List<TestTrainProject>> queryParentProjectListByLessonId(Long lessonId) {
                log.error("Method: {}\nrequest:{}\ncause: {}", "ITestTrainProjectService.queryParentProjectListByLessonId",
                    lessonId,
                    Throwables.getStackTraceAsString(throwable));

                return null;
            }

        };
    }
}