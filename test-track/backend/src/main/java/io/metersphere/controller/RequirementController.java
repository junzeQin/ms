package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileMetadataWithBLOBs;
import io.metersphere.base.domain.Requirement;
import io.metersphere.base.domain.RequirementWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.RequirementDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.metadata.vo.DumpFileRequest;
import io.metersphere.metadata.vo.FileResponse;
import io.metersphere.metadata.vo.MoveFIleMetadataRequest;
import io.metersphere.request.QueryProjectFileRequest;
import io.metersphere.request.requirement.RequirementCreateRequest;
import io.metersphere.service.RequirementService;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

@RequestMapping("/requirement")
@RestController
public class RequirementController {
    @Resource
    private RequirementService requirementService;

    @GetMapping(value = "/info/{fileId}")
    @RequiresPermissions("PROJECT_FILE:READ")
    public FileResponse image(@PathVariable("fileId") String fileId) {
        FileResponse fileResponse = new FileResponse();
        fileResponse.setBytes(requirementService.getFile(fileId).getBody());
        return fileResponse;
    }

    /**
     * 查询列表
     * @param projectId
     * @param goPage
     * @param pageSize
     * @param request
     * @return
     */
    @PostMapping("/project/{projectId}/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_FILE:READ")
    public Pager<List<RequirementDTO>> getProjectFiles(@PathVariable String projectId, @PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryProjectFileRequest request) {
        //requirementService.checkProjectFileHasModuleId(projectId);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, requirementService.getRequirementByProject(projectId, request));
    }

    /**
     * 上传文件
     * @param request
     * @param files
     * @return
     */
    @PostMapping(value = "/create")
    @RequiresPermissions("PROJECT_FILE:READ+UPLOAD+JAR")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = RequirementService.class)
    public List<Requirement> create(@RequestPart("request") RequirementCreateRequest request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return requirementService.create(request, files);
    }

    @PostMapping(value = "/upload")
    @RequiresPermissions("PROJECT_FILE:READ+UPLOAD+JAR")
    public FileMetadata upload(@RequestPart("request") FileMetadataWithBLOBs request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return requirementService.reLoad(request, files);
    }

    @GetMapping(value = "/download/{id}")
    @RequiresPermissions("PROJECT_FILE:READ+DOWNLOAD+JAR")
    public ResponseEntity<byte[]> download(@PathVariable("id") String id) {
        return requirementService.getFile(id);
    }

    /**
     * 预览文件
     * @param id
     * @return
     */
    @GetMapping(value = "/preview/{id}")
    @RequiresPermissions("PROJECT_FILE:READ")
    public ResponseEntity<byte[]> preview(@PathVariable("id") String id) {
        return requirementService.getPreviewFile(id);
    }

//    @PostMapping(value = "/download/zip")
//    @RequiresPermissions("PROJECT_FILE:READ+DOWNLOAD+JAR")
//    public ResponseEntity<byte[]> downloadBodyFiles(@RequestBody DownloadRequest request) {
//        try {
//            byte[] bytes = requirementService.exportZip(request);
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType("application/octet-stream"))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "fileList.zip")
//                    .body(bytes);
//        } catch (Exception e) {
//            return ResponseEntity.status(509).body(e.getMessage().getBytes());
//        }
//    }

    @GetMapping(value = "/delete/{fileId}")
    @RequiresPermissions("PROJECT_FILE:READ+DELETE+JAR")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#fileId)", msClass = RequirementService.class)
    public void deleteFile(@PathVariable String fileId) {
        requirementService.deleteFile(fileId);
    }

    @PostMapping(value = "/delete/batch")
    @RequiresPermissions("PROJECT_FILE:READ+BATCH+DELETE")
    public void deleteBatch(@RequestBody List<String> ids) {
        requirementService.deleteBatch(ids);
    }

    @GetMapping(value = "/get/type/all")
    public List<String> getTypes() {
        return requirementService.getTypes();
    }

    @PostMapping(value = "/move")
    @RequiresPermissions("PROJECT_FILE:READ+BATCH+MOVE")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = RequirementService.class)
    public void move(@RequestBody MoveFIleMetadataRequest request) {
        requirementService.move(request);
    }

    @PostMapping(value = "/update")
    @RequiresPermissions("PROJECT_FILE:READ+UPLOAD+JAR")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = RequirementService.class)
    public void update(@RequestBody RequirementWithBLOBs request) {
        requirementService.update(request);
    }

    @PostMapping(value = "/dump/file", consumes = {"multipart/form-data"})
    @RequiresPermissions("PROJECT_FILE:READ+UPLOAD+JAR")
    public void dumpFile(@RequestPart("request") DumpFileRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        requirementService.dumpFile(request, files);
    }

    @GetMapping(value = "/count/{projectId}/{createUser}")
    @RequiresPermissions("PROJECT_FILE:READ")
    public long myFiles(@PathVariable String projectId, @PathVariable String createUser) {
        return requirementService.myFiles(createUser, projectId);
    }

    @GetMapping(value = "/exist/{fileId}")
    @RequiresPermissions("PROJECT_FILE:READ")
    public boolean exist(@PathVariable("fileId") String fileId) {
        return requirementService.exist(fileId);
    }

    @PostMapping(value = "/exists")
    @RequiresPermissions("PROJECT_FILE:READ")
    public List<String> exist(@RequestBody List<String> fileIds) {
        return requirementService.exists(fileIds);
    }

}
