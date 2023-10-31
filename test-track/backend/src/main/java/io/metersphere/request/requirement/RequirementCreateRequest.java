package io.metersphere.request.requirement;

import io.metersphere.base.domain.RequirementWithBLOBs;

import lombok.Data;

@Data
public class RequirementCreateRequest extends RequirementWithBLOBs {
    private String repositoryBranch;
    private String repositoryPath;
}
