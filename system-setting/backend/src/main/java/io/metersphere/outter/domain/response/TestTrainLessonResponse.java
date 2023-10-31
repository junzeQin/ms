package io.metersphere.outter.domain.response;

import io.metersphere.cdc.domain.model.TestTrainLesson;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestTrainLessonResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private TestTrainLesson lesson;

}
