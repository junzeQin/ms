package io.metersphere.base.mapper;

import io.metersphere.base.domain.Requirement;
import io.metersphere.base.domain.RequirementExample;
import io.metersphere.base.domain.RequirementWithBLOBs;
import io.metersphere.dto.RequirementDTO;
import io.metersphere.metadata.vo.MoveFIleMetadataRequest;
import io.metersphere.request.QueryProjectFileRequest;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface RequirementMapper {
    List<RequirementWithBLOBs> getProjectFiles(@Param("projectId") String projectId, @Param("request") QueryProjectFileRequest request);

    List<RequirementDTO> getRequirementByProject(@Param("projectId") String projectId, @Param("request") QueryProjectFileRequest request);

    List<String> getTypes();

    void move(@Param("request") MoveFIleMetadataRequest request);

    List<Map<String, Object>> moduleCountByMetadataIds(@Param("ids") List<String> ids);

    void updateModuleIdByProjectId(@Param("moduleId") String moduleId, @Param("projectId") String projectId);

    List<String> selectRefIdsByIds(@Param("ids") List<String> nodeIds);

    List<String> selectIllegalModuleIdListByProjectId(String projectId);

    List<RequirementWithBLOBs> selectByExampleWithBLOBs(RequirementExample example);

    int insert(RequirementWithBLOBs record);

    long countByExample(RequirementExample example);

    int updateByPrimaryKeyWithBLOBs(RequirementWithBLOBs record);

    RequirementWithBLOBs selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(RequirementWithBLOBs record);

    List<Requirement> selectByExample(RequirementExample example);

    int deleteByPrimaryKey(String id);

    int deleteByExample(RequirementExample example);
}