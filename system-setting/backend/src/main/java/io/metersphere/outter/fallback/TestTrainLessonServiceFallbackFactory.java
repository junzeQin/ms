package io.metersphere.outter.fallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import io.metersphere.outter.ITestTrainLessonService;
import io.metersphere.outter.domain.request.TestTrainLessonTeamUserRequest;
import io.metersphere.outter.domain.request.TestTrainLessonUserRequest;
import io.metersphere.outter.domain.response.TestTrainLessonResponse;
import io.metersphere.outter.domain.response.TestTrainLessonTeamUserResponse;
import io.metersphere.outter.domain.response.TestTrainLessonUserResponse;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestTrainLessonServiceFallbackFactory implements FallbackFactory<ITestTrainLessonService> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ITestTrainLessonService create(Throwable throwable) {
        return new ITestTrainLessonService() {

            @Override
            public TestTrainLessonResponse getLessonById(Long lessonId) {
                log.error("Method: {}\nrequest:{}\ncause: {}", "ITestTrainService.getLessonById",
                    lessonId,
                    Throwables.getStackTraceAsString(throwable));

                return null;
            }

            @SneakyThrows
            @Override
            public TestTrainLessonUserResponse queryLessonUser(TestTrainLessonUserRequest request) {
                log.error("Method: {}\nrequest:{}\ncause: {}", "ITestTrainService.queryLessonUser",
                    objectMapper.writeValueAsString(request),
                    Throwables.getStackTraceAsString(throwable));

                return null;
            }

            @SneakyThrows
            @Override
            public TestTrainLessonTeamUserResponse queryLessonTeamUser(TestTrainLessonTeamUserRequest request) {
                log.error("Method: {}\nrequest:{}\ncause: {}", "ITestTrainService.queryLessonTeamUser",
                    objectMapper.writeValueAsString(request),
                    Throwables.getStackTraceAsString(throwable));

                return null;
            }

        };
    }
}
