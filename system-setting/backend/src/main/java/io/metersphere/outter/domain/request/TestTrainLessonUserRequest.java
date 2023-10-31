package io.metersphere.outter.domain.request;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestTrainLessonUserRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long lessonId;

    private Long role;

}
