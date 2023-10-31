package io.metersphere.request.member;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class AddMemberRequest {

    private String workspaceId;

    private List<String> userIds;

    private List<String> roleIds;

    private List<String> groupIds;

    private String projectId;

}
