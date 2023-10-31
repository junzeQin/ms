package io.metersphere.outter;

import io.metersphere.outter.domain.request.TestTrainLessonTeamUserRequest;
import io.metersphere.outter.domain.request.TestTrainLessonUserRequest;
import io.metersphere.outter.domain.response.TestTrainLessonResponse;
import io.metersphere.outter.domain.response.TestTrainLessonTeamUserResponse;
import io.metersphere.outter.domain.response.TestTrainLessonUserResponse;
import io.metersphere.outter.fallback.TestTrainLessonServiceFallbackFactory;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "testTrainLessonService", url = "${feign.testTrain.url}", fallbackFactory = TestTrainLessonServiceFallbackFactory.class)
public interface ITestTrainLessonService {

    @GetMapping("/train/lesson/get/{lessonId}")
    @Retryable(include = {Exception.class}, backoff = @Backoff(value = 1000, multiplier = 1.5))
    TestTrainLessonResponse getLessonById(@PathVariable("lessonId") Long lessonId);

    @PostMapping({"/train/member/queryLessonUser"})
    @Retryable(include = {Exception.class}, backoff = @Backoff(value = 1000, multiplier = 1.5))
    TestTrainLessonUserResponse queryLessonUser(TestTrainLessonUserRequest request);

    @PostMapping({"/train/user/queryLessonTeamUser"})
    @Retryable(include = {Exception.class}, backoff = @Backoff(value = 1000, multiplier = 1.5))
    TestTrainLessonTeamUserResponse queryLessonTeamUser(TestTrainLessonTeamUserRequest request);

}
