package io.metersphere.request.member;

import io.metersphere.base.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest extends User {

    /**
     * 弃用
     */
    @Deprecated
    private List<Map<String, Object>> roles = new ArrayList<>();

    private List<Map<String, Object>> groups = new ArrayList<>();

    private boolean passwordEncode = true;

}
