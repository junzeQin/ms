package io.metersphere.outter;

import io.metersphere.cdc.domain.model.TestTrainProject;
import io.metersphere.outter.domain.R;
import io.metersphere.outter.domain.response.TestTrainProjectResponse;
import io.metersphere.outter.fallback.TestTrainProjectServiceFallbackFactory;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "testTrainProjectService", url = "${feign.testTrain.url}", fallbackFactory = TestTrainProjectServiceFallbackFactory.class)
public interface ITestTrainProjectService {

    @GetMapping({"/train/project/getProject/{projectId}"})
    @Retryable(include = {Exception.class}, backoff = @Backoff(value = 1000, multiplier = 1.5))
    TestTrainProjectResponse getProjectById(@PathVariable("projectId") Long projectId);

    @GetMapping({"/train/project/queryParentProjectList/{lessonId}"})
    @Retryable(include = {Exception.class}, backoff = @Backoff(value = 1000, multiplier = 1.5))
    R<List<TestTrainProject>> queryParentProjectListByLessonId(@PathVariable("lessonId") Long lessonId);

}
