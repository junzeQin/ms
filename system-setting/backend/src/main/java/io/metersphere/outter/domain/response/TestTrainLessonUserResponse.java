package io.metersphere.outter.domain.response;

import io.metersphere.cdc.domain.model.TestTrainLessonUser;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestTrainLessonUserResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<TestTrainLessonUser> lessonUserList;

}
