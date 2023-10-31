package io.metersphere.service;


import io.metersphere.base.domain.FileAssociationExample;
import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileMetadataExample;
import io.metersphere.base.domain.FileMetadataWithBLOBs;
import io.metersphere.base.domain.FileModule;
import io.metersphere.base.domain.Requirement;
import io.metersphere.base.domain.RequirementExample;
import io.metersphere.base.domain.RequirementWithBLOBs;
import io.metersphere.base.mapper.FileAssociationMapper;
import io.metersphere.base.mapper.FileContentMapper;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.base.mapper.RequirementMapper;
import io.metersphere.commons.constants.FileModuleTypeConstants;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.AttachmentBodyFile;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.dto.RequirementDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.metadata.service.FileManagerService;
import io.metersphere.metadata.service.FileModuleService;
import io.metersphere.metadata.utils.GitRepositoryUtil;
import io.metersphere.metadata.utils.MetadataUtils;
import io.metersphere.metadata.vo.DumpFileRequest;
import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.metadata.vo.MoveFIleMetadataRequest;
import io.metersphere.metadata.vo.RemoteFileAttachInfo;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.QueryProjectFileRequest;
import io.metersphere.request.requirement.RequirementCreateRequest;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.TemporaryFileUtil;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

@Service
public class RequirementService {
    @Resource
    private RequirementMapper requirementMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileManagerService fileManagerService;
    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private OpenofficePdfService openofficePdfService;

    private TemporaryFileUtil temporaryFileUtil;

    public RequirementService() {
        super();
        temporaryFileUtil = new TemporaryFileUtil(TemporaryFileUtil.MS_FILE_FOLDER);
    }

    public List<Requirement> create(RequirementCreateRequest requirement, List<MultipartFile> files) {
        List<Requirement> result = new ArrayList<>();
        if (requirement == null) {
            requirement = new RequirementCreateRequest();
        }
        if (StringUtils.equals(StorageConstants.GIT.name(), requirement.getStorage())) {
            requirement.setPath(StringUtils.trim(requirement.getPath()));
            this.validateGitFile(requirement);
            GitRepositoryUtil repositoryUtils = new GitRepositoryUtil(null, null, null);
            RemoteFileAttachInfo gitFileInfo = repositoryUtils.selectLastCommitIdByBranch(requirement.getRepositoryBranch(), requirement.getRepositoryPath());
            if (gitFileInfo != null) {
                requirement.setName(MetadataUtils.getFileNameByRemotePath(requirement.getRepositoryPath()));
                requirement.setType(MetadataUtils.getFileType(requirement.getRepositoryPath()));
                requirement.setPath(requirement.getRepositoryPath());
                requirement.setSize(gitFileInfo.getSize());
                requirement.setAttachInfo(JSON.toJSONString(gitFileInfo));
                result.add(this.save(requirement));
            } else {
                MSException.throwException("File not found!");
            }
        } else if (!CollectionUtils.isEmpty(files)) {
            for (MultipartFile file : files) {
                QueryProjectFileRequest request = new QueryProjectFileRequest();
                request.setName(file.getOriginalFilename());
                result.add(this.saveFile(file, requirement));
            }
        }
        return result;
    }

    private void validateGitFile(RequirementCreateRequest validateModel) {
        RequirementExample example = new RequirementExample();
        example.createCriteria().andStorageEqualTo(validateModel.getStorage())
                .andPathEqualTo(validateModel.getRepositoryPath())
                .andIdNotEqualTo(validateModel.getId());
        List<RequirementWithBLOBs> requirementWithBLOBsList = requirementMapper.selectByExampleWithBLOBs(example);
        for (RequirementWithBLOBs requirementWithBLOBs : requirementWithBLOBsList) {
            RemoteFileAttachInfo gitFileInfo = null;
            try {
                gitFileInfo = JSON.parseObject(requirementWithBLOBs.getAttachInfo(), RemoteFileAttachInfo.class);
            } catch (Exception e) {
                LogUtil.error("解析git信息失败!", e);
            }
            if (StringUtils.equals(gitFileInfo.getBranch(), validateModel.getRepositoryBranch())) {
                MSException.throwException(Translator.get("project_file_already_exists"));
            }
        }
    }

    public Requirement save(RequirementWithBLOBs requirement) {
        long createTime = System.currentTimeMillis();
        requirement.setCreateTime(createTime);
        requirement.setUpdateTime(createTime);
        requirement.setLatest(true);
        requirement.setRefId(requirement.getId());
        requirementMapper.insert(requirement);
        return requirement;
    }

    public Requirement saveFile(MultipartFile file, RequirementWithBLOBs requirement) {
        this.initBase(requirement);
        if (StringUtils.isEmpty(requirement.getName())) {
            requirement.setName(file.getOriginalFilename());
        }
        checkName(requirement);
        requirement.setSize(file.getSize());
        String fileType = MetadataUtils.getFileType(requirement.getName());
        requirement.setType(fileType);
        // 上传文件
        FileRequest request = new FileRequest(requirement.getProjectId(), "requirement/"+requirement.getName(), requirement.getType());
        String path = fileManagerService.upload(file, request);

        if (StringUtils.isBlank(path)) {
            MSException.throwException(Translator.get("upload_file_fail_get_file_path_fail"));
        }else{
            // word文件转pdf
            if(fileType.equals("DOCX") || fileType.equals("DOC")){
                try{
                    // PDF上传到文件服务器
                    int lastDotIndex = requirement.getName().lastIndexOf('.');
                    String name = requirement.getName().substring(0, lastDotIndex);
                    File pdfFile = openofficePdfService.convert(file.getInputStream(), name, fileType);
                    request = new FileRequest(requirement.getProjectId(), "requirement/"+name+".pdf", "pdf");
                    String pdfpath = fileManagerService.upload(Files.readAllBytes(pdfFile.toPath()), request);
                    requirement.setPdfPath(pdfpath);
                }catch (Exception e){

                }
            }
            // pdf文件直接预览原文件
            if(fileType.equals("PDF")){
                requirement.setPdfPath(path);
            }
        }
        requirement.setPath(path);
        if (fileMetadataMapper.selectByPrimaryKey(requirement.getId()) == null) {
            requirement.setLatest(true);
            requirement.setRefId(requirement.getId());
            requirementMapper.insert(requirement);
        } else {
            requirementMapper.updateByPrimaryKeyWithBLOBs(requirement);
        }

        return requirement;
    }


    public Requirement saveFile(MultipartFile file, String projectId) {
        RequirementWithBLOBs requirement = new RequirementWithBLOBs();
        requirement.setProjectId(projectId);
        return saveFile(file, requirement);
    }

    public List<RequirementDTO> getRequirementByProject(String projectId, QueryProjectFileRequest request) {
        if (CollectionUtils.isEmpty(request.getOrders())) {
            OrderRequest req = new OrderRequest();
            req.setName("update_time");
            req.setType("desc");
            request.setOrders(new ArrayList<>() {{
                this.add(req);
            }});
        }
        return requirementMapper.getRequirementByProject(projectId, request);
    }

    public List<RequirementWithBLOBs> getProjectFiles(String projectId, QueryProjectFileRequest request) {
        if (CollectionUtils.isEmpty(request.getOrders())) {
            OrderRequest req = new OrderRequest();
            req.setName("update_time");
            req.setType("desc");
            request.setOrders(new ArrayList<>() {{
                this.add(req);
            }});
        }
        return requirementMapper.getProjectFiles(projectId, request);
    }

    public void deleteFile(String fileId) {
        if (StringUtils.isEmpty(fileId)) {
            return;
        }
        // 删除文件引用关系
        FileAssociationExample associationExample = new FileAssociationExample();
        associationExample.createCriteria().andFileMetadataIdEqualTo(fileId);
        fileAssociationMapper.deleteByExample(associationExample);

        // 删除文件,历史遗留数据保留附件只删除关系
        Requirement requirement = requirementMapper.selectByPrimaryKey(fileId);
        requirementMapper.deleteByPrimaryKey(fileId);

        if (StringUtils.isNotEmpty(requirement.getRefId())) {
            //删除其余版本的文件
            RequirementExample requirementExample = new RequirementExample();
            requirementExample.createCriteria().andRefIdEqualTo(requirement.getRefId());
            requirementMapper.deleteByExample(requirementExample);
        }

        // 删除数据库里保存的文件内容
        fileContentMapper.deleteByPrimaryKey(fileId);

        if (StringUtils.isNotEmpty(requirement.getStorage()) && StringUtils.isEmpty(requirement.getResourceType())) {
            FileRequest request = new FileRequest(requirement.getProjectId(), "requirement/"+requirement.getName(), requirement.getType());
            fileManagerService.delete(request);
            // 存在PDF文件进行删除
            if(StringUtils.isNotBlank(requirement.getPdfPath())){
                int lastDotIndex = requirement.getName().lastIndexOf('.');
                String name = requirement.getName().substring(0, lastDotIndex);
                request = new FileRequest(requirement.getProjectId(), "requirement/"+name+".pdf", "pdf");
                fileManagerService.delete(request);
            }
        }
    }

    public void deleteBatch(List<String> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            ids.forEach(item -> {
                deleteFile(item);
            });
        }
    }

    public List<String> getTypes() {
        return requirementMapper.getTypes();
    }

    public void move(MoveFIleMetadataRequest request) {
        //不可移动到存储库模块节点
        FileModule fileModule = fileModuleService.get(request.getModuleId());
        if (fileModule != null && !CollectionUtils.isEmpty(request.getMetadataIds()) && StringUtils.isNotEmpty(request.getModuleId())) {
            if (StringUtils.equals(fileModule.getModuleType(), FileModuleTypeConstants.REPOSITORY.getValue())) {
                MSException.throwException(Translator.get("can_not_move_to_repository_node"));
            } else {
                requirementMapper.move(request);
            }
        }
    }

    public byte[] getContent(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);
        if (fileContent != null) {
            return fileContent.getFile();
        }
        return null;
    }

    public FileMetadataWithBLOBs selectById(String id) {
        return fileMetadataMapper.selectByPrimaryKey(id);
    }

    public byte[] loadFileAsBytes(String id) {
        RequirementWithBLOBs requirement = requirementMapper.selectByPrimaryKey(id);
        if (requirement == null) {
            return new byte[0];
        }
        return this.loadFileAsBytes(requirement);
    }

    public byte[] loadFileAsBytes(RequirementWithBLOBs fileMetadata) {
        byte[] bytes = new byte[0];
        // 兼容历史数据
        if (StringUtils.isEmpty(fileMetadata.getStorage()) && StringUtils.isEmpty(fileMetadata.getResourceType())) {
            bytes = getContent(fileMetadata.getId());
        }
        if (bytes == null || bytes.length == 0) {
            FileRequest request = new FileRequest(fileMetadata.getProjectId(), "requirement/"+fileMetadata.getName(), fileMetadata.getType());
            request.setResourceType(fileMetadata.getResourceType());
            request.setPath(fileMetadata.getPath());
            request.setStorage(fileMetadata.getStorage());
            request.setFileAttachInfoByString(fileMetadata.getAttachInfo());
            request.setUpdateTime(fileMetadata.getUpdateTime());
            bytes = fileManagerService.downloadFile(request);
        }

        return Objects.requireNonNullElseGet(bytes, () -> new byte[0]);
    }

    public ResponseEntity<byte[]> getFile(String fileId) {
        MediaType contentType = MediaType.parseMediaType("application/octet-stream");
        RequirementWithBLOBs requirement = requirementMapper.selectByPrimaryKey(fileId);
        if (requirement == null) {
            return null;
        }
        byte[] bytes = loadFileAsBytes(requirement);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + MetadataUtils.getFileName(requirement.getName(), requirement.getType()) + "\"")
                .body(bytes);
    }

    public ResponseEntity<byte[]> getPreviewFile(String fileId) {
        MediaType contentType = MediaType.parseMediaType("application/pdf");
        RequirementWithBLOBs requirement = requirementMapper.selectByPrimaryKey(fileId);
        if (requirement == null) {
            return null;
        }
        int lastDotIndex = requirement.getName().lastIndexOf('.');
        String name = requirement.getName().substring(0, lastDotIndex);
        byte[] bytes = loadPreviewFileAsBytes(requirement);
        return ResponseEntity.ok()
            .contentType(contentType)
//            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name+".pdf" + "\"")
            .body(bytes);
    }

    public byte[] loadPreviewFileAsBytes(RequirementWithBLOBs requirement) {
        byte[] bytes = new byte[0];
        // 兼容历史数据
        if (StringUtils.isEmpty(requirement.getStorage()) && StringUtils.isEmpty(requirement.getResourceType())) {
            bytes = getContent(requirement.getId());
        }
        if (bytes == null || bytes.length == 0) {

            int lastDotIndex = requirement.getName().lastIndexOf('.');
            String name = requirement.getName().substring(0, lastDotIndex);
            FileRequest request = new FileRequest(requirement.getProjectId(), "requirement/"+name+".pdf", "pdf");
            request.setResourceType(requirement.getResourceType());
            request.setPath(requirement.getPath());
            request.setStorage(requirement.getStorage());
            request.setFileAttachInfoByString(requirement.getAttachInfo());
            request.setUpdateTime(requirement.getUpdateTime());
            bytes = fileManagerService.downloadFile(request);
        }

        return Objects.requireNonNullElseGet(bytes, () -> new byte[0]);
    }

//    public byte[] exportZip(DownloadRequest request) {
//        Map<String, byte[]> files = new LinkedHashMap<>();
//        request.getRequests().forEach(requirement -> {
//            byte[] bytes = loadFileAsBytes(requirement);
//            if (bytes != null) {
//                files.put(MetadataUtils.getFileName(requirement.getName(), requirement.getType()), bytes);
//            }
//        });
//
//        return FileUtils.listBytesToZip(files);
//    }


    private void checkName(Requirement requirement) {
        //文件库的名字不用判断是否唯一.是根据文件库路径自动解析生成的。
        if (!StringUtils.equals(StorageConstants.GIT.name(), requirement.getStorage())) {
            RequirementExample example = new RequirementExample();
            RequirementExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(requirement.getName())
                    .andProjectIdEqualTo(requirement.getProjectId())
                    .andIdNotEqualTo(requirement.getId());
            if (requirementMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("project_file_already_exists"));
            }
        }
        // 文件名不得超过250个字符
        if (requirement.getName().length() > 250) {
            MSException.throwException(Translator.get("project_file_name_too_long"));
        }
    }

    private String getBeforeName(Requirement requirement) {
        return requirementMapper.selectByPrimaryKey(requirement.getId()).getName();
    }

    public void update(RequirementWithBLOBs requirement) {
        this.checkName(requirement);
        if (!this.isFileChanged(requirement)) {
            //文件未改变时不会触发保存逻辑
            return;
        }
        String beforeName = getBeforeName(requirement);
        if (!StringUtils.equalsIgnoreCase(beforeName, requirement.getName())
                && StringUtils.isNotEmpty(requirement.getStorage()) && StringUtils.isEmpty(requirement.getResourceType())) {
            boolean isReName = fileManagerService.reName(beforeName, requirement.getName(), requirement.getProjectId());
            if (!isReName) {
                MSException.throwException("重命名失败！");
            }
        }
        requirement.setUpdateTime(System.currentTimeMillis());
        requirement.setUpdateUser(SessionUtils.getUserId());
        // 历史数据和Git数据的路径不做更新
        if (!StringUtils.equalsIgnoreCase(StorageConstants.GIT.name(), requirement.getStorage())
            && (StringUtils.isNotEmpty(requirement.getStorage()) && StringUtils.isEmpty(requirement.getResourceType()))) {
            requirement.setPath(FileUtils.getFilePath(requirement));
        }
        //latest字段只能在git/pull时更新
        requirement.setLatest(null);
        requirementMapper.updateByPrimaryKeySelective(requirement);
    }

    public boolean isFileChanged(RequirementWithBLOBs newFile) {
        FileMetadataWithBLOBs oldFile = this.getFileMetadataById(newFile.getId());
        if (oldFile != null) {
            if (StringUtils.equals("[]", oldFile.getTags())) {
                oldFile.setTags(null);
            }
            if (StringUtils.equals("[]", newFile.getTags())) {
                newFile.setTags(null);
            }

            return !StringUtils.equals(newFile.getDescription(), oldFile.getDescription())
                    || !StringUtils.equals(newFile.getAttachInfo(), oldFile.getAttachInfo())
                    || !StringUtils.equals(newFile.getName(), oldFile.getName())
                    || !StringUtils.equals(newFile.getType(), oldFile.getType())
                    || !StringUtils.equals(newFile.getProjectId(), oldFile.getProjectId())
                    || !StringUtils.equals(newFile.getStorage(), oldFile.getStorage())
                    || !StringUtils.equals(newFile.getTags(), oldFile.getTags())
                    || !StringUtils.equals(newFile.getPath(), oldFile.getPath())
                    || !StringUtils.equals(newFile.getResourceType(), oldFile.getResourceType())
                    || !StringUtils.equals(newFile.getRefId(), oldFile.getRefId())
                    || !StringUtils.equals(String.valueOf(newFile.getSize()), String.valueOf(oldFile.getSize()))
                    || (newFile.getLatest() != oldFile.getLatest())
                    || (newFile.getLoadJar() != oldFile.getLoadJar());
        }
        return true;
    }

    public FileMetadata reLoad(FileMetadataWithBLOBs fileMetadata, List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            return fileMetadata;
        }
        fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileMetadata.getId());
        if (fileMetadata == null) {
            MSException.throwException("数据已经被删除！");
        }
        fileMetadata.setSize(files.get(0).getSize());
        String fileType = MetadataUtils.getFileType(files.get(0).getOriginalFilename());
        fileMetadata.setType(fileType);
        if (StringUtils.isEmpty(fileMetadata.getStorage())) {
            fileMetadata.setStorage(StorageConstants.MINIO.name());
        }
        // 上传文件
        FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
        String path = fileManagerService.coverFile(files.get(0), request);
        // 更新关系数据
        fileMetadata.setPath(path);
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        fileMetadata.setUpdateUser(SessionUtils.getUserId());
        fileMetadataMapper.updateByPrimaryKeySelective(fileMetadata);
        return fileMetadata;
    }

    public FileMetadataWithBLOBs getFileMetadataById(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId);
    }

    public List<Requirement> uploadFiles(String projectId, List<MultipartFile> files) {
        List<Requirement> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(files)) {
            RequirementWithBLOBs requirement = new RequirementWithBLOBs();
            requirement.setProjectId(projectId);
            requirement.setStorage(StorageConstants.MINIO.name());
            files.forEach(file -> {
                QueryProjectFileRequest request = new QueryProjectFileRequest();
                request.setName(file.getOriginalFilename());
                if (CollectionUtils.isEmpty(this.getProjectFiles(requirement.getProjectId(), request))) {
                    result.add(this.saveFile(file, requirement));
                } else {
                    MSException.throwException(Translator.get("project_file_already_exists"));
                }
            });
        }
        return result;
    }

    public FileMetadata updateFile(String fileId, MultipartFile file) {
        FileMetadataWithBLOBs fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        reLoad(fileMetadata, new ArrayList<>() {{
            this.add(file);
        }});
        return fileMetadata;
    }

    public void dumpFile(DumpFileRequest request, List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            // 文件已经存储过了
            String path = StringUtils.join(FileUtils.BODY_FILE_DIR, File.separator, request.getResourceId(), File.separator, request.getFileName());
            if (request.isCsv()) {
                path = StringUtils.join(FileUtils.BODY_FILE_DIR, File.separator, request.getResourceId(), "_", request.getFileName());
            }
            File file = new File(path);
            if (!file.exists()) {
                MSException.throwException("文件不存在！");
            }
            if (request.isCsv()) {
                this.saveFile(file, request.getFileName(), request.getModuleId());
            } else {
                this.saveFile(file);
            }
        } else {
            RequirementCreateRequest requirement = new RequirementCreateRequest();
            requirement.setProjectId(request.getProjectId());
            this.create(requirement, files);
        }
    }

    public List<String> getJar(List<String> projectIds) {
        if (!CollectionUtils.isEmpty(projectIds)) {
            FileMetadataExample fileMetadata = new FileMetadataExample();
            fileMetadata.createCriteria().andProjectIdIn(projectIds).andLoadJarEqualTo(true);
            List<FileMetadata> files = fileMetadataMapper.selectByExample(fileMetadata);
            files = files.stream().filter(s -> StringUtils.isNotEmpty(s.getPath())).collect(Collectors.toList());
            return files.stream().map(FileMetadata::getPath).collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    public long myFiles(String createUser, String projectId) {
        FileMetadataExample fileMetadata = new FileMetadataExample();
        fileMetadata.createCriteria().andProjectIdEqualTo(projectId).andCreateUserEqualTo(createUser);
        return fileMetadataMapper.countByExample(fileMetadata);
    }

    public String getLogDetails(String id) {
        FileMetadata fileMetadata = this.getFileMetadataById(id);
        if (fileMetadata != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(fileMetadata, SystemReference.projectColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(fileMetadata.getId()), fileMetadata.getProjectId(), fileMetadata.getName(), null, columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public boolean isFileExits(String fileId) {
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdEqualTo(fileId);
        long fileCount = fileMetadataMapper.countByExample(example);
        if (fileCount > 0) {
            return true;
        } else {
            return false;
        }
    }

    public FileMetadata saveFile(File file) {
        if (file.exists()) {
            byte[] bytes = FileUtils.fileToByte(file);
            this.saveFile(bytes, file.getName(), file.length());
        }
        return null;
    }

    public void saveFile(File file, String name, String moduleId) {
        if (file.exists()) {
            byte[] bytes = FileUtils.fileToByte(file);
            this.saveLocalFile(bytes, name, file.length(), moduleId);
        }
    }

    public Requirement saveFile(byte[] fileByte, String fileName, Long fileSize) {
        final RequirementWithBLOBs requirement = new RequirementWithBLOBs();
        this.initBase(requirement);
        requirement.setName(fileName);
        RequirementExample example = new RequirementExample();
        example.createCriteria().andProjectIdEqualTo(requirement.getProjectId()).andNameEqualTo(requirement.getName());
        List<Requirement> list = requirementMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        } else {
            requirement.setSize(fileSize);
            String fileType = MetadataUtils.getFileType(fileName);
            requirement.setType(fileType);
            checkName(requirement);
            FileRequest request = new FileRequest(requirement.getProjectId(), requirement.getName(), requirement.getType());
            String path = fileManagerService.upload(fileByte, request);
            requirement.setPath(path);
            requirement.setLatest(true);
            requirement.setRefId(requirement.getId());
            requirementMapper.insert(requirement);
            return requirement;
        }
    }

    public void saveLocalFile(byte[] fileByte, String fileName, Long fileSize, String moduleId) {
        final RequirementWithBLOBs requirement = new RequirementWithBLOBs();
        this.initBase(requirement);
        requirement.setName(fileName);
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andProjectIdEqualTo(requirement.getProjectId()).andNameEqualTo(requirement.getName());
        List<FileMetadata> list = fileMetadataMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
        } else {
            requirement.setSize(fileSize);
            String fileType = MetadataUtils.getFileType(fileName);
            requirement.setType(fileType);
            checkName(requirement);
            FileRequest request = new FileRequest(requirement.getProjectId(), requirement.getName(), requirement.getType());
            String path = fileManagerService.upload(fileByte, request);
            requirement.setPath(path);
            requirement.setLatest(true);
            requirement.setRefId(requirement.getId());
            requirementMapper.insert(requirement);
        }
    }

    public void initBase(Requirement requirement) {
        if (requirement == null) {
            requirement = new Requirement();
        }
        if (StringUtils.isEmpty(requirement.getId())) {
            requirement.setId(UUID.randomUUID().toString());
        }
        if (StringUtils.isEmpty(requirement.getStorage())) {
            requirement.setStorage(StorageConstants.MINIO.name());
        }
        if (StringUtils.isEmpty(requirement.getProjectId())) {
            requirement.setProjectId(SessionUtils.getCurrentProjectId());
        }
        requirement.setCreateTime(System.currentTimeMillis());
        requirement.setUpdateTime(System.currentTimeMillis());

        if (StringUtils.isEmpty(requirement.getCreateUser())) {
            requirement.setCreateUser(SessionUtils.getUserId());
        }
        if (StringUtils.isEmpty(requirement.getUpdateUser())) {
            requirement.setUpdateUser(SessionUtils.getUserId());
        }
    }

    public boolean exist(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId) != null;
    }

    public List<String> exists(List<String> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return new LinkedList<>();
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        List<FileMetadata> fileMetadataList = fileMetadataMapper.selectByExample(example);
        return fileMetadataList.stream().map(FileMetadata::getId).collect(Collectors.toList());
    }

    public List<AttachmentBodyFile> filterDownloadFileList(List<AttachmentBodyFile> attachmentBodyFileList) {
        List<AttachmentBodyFile> downloadFileList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(attachmentBodyFileList)) {
            //检查是否存在已下载的文件
            attachmentBodyFileList.forEach(fileMetadata -> {
                if (!StringUtils.equals(fileMetadata.getFileStorage(), StorageConstants.LOCAL.name())) {
                    File file = temporaryFileUtil.getFile(fileMetadata.getProjectId(), fileMetadata.getFileMetadataId(), fileMetadata.getFileUpdateTime(), fileMetadata.getName(), fileMetadata.getFileType());
                    if (file == null) {
                        downloadFileList.add(fileMetadata);
                        LoggerUtil.info("文件【" + fileMetadata.getFileUpdateTime() + "_" + fileMetadata.getName() + "】在执行目录【" + fileMetadata.getProjectId() + "】未找到，需要下载");
                    }
                }
            });
        }
        return downloadFileList;
    }

    /**
     * 提供给Node下载附件时的方法。
     * 该方法会优先判断是否存在已下载好的文件，避免多次执行造成多次下载的情况
     *
     * @param fileIdList 要下载的文件ID集合
     * @return
     */
    public List<FileInfoDTO> downloadApiExecuteFilesByIds(Collection<String> fileIdList) {
        List<FileInfoDTO> fileInfoDTOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(fileIdList)) {
            return fileInfoDTOList;
        }
        LogUtil.info(JSON.toJSONString(fileIdList) + " 获取执行文件开始");
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(new ArrayList<>(fileIdList));
        List<FileMetadataWithBLOBs> fileMetadataWithBLOBList = fileMetadataMapper.selectByExampleWithBLOBs(example);
        List<FileRequest> downloadFileRequest = new ArrayList<>();
        //检查是否存在已下载的文件
        fileMetadataWithBLOBList.forEach(fileMetadata -> {
            File file = temporaryFileUtil.getFile(fileMetadata.getProjectId(), fileMetadata.getId(), fileMetadata.getUpdateTime(), fileMetadata.getName(), fileMetadata.getType());
            if (file != null) {
                LoggerUtil.info("文件【" + fileMetadata.getUpdateTime() + "_" + fileMetadata.getName() + "】在执行目录【" + fileMetadata.getProjectId() + "】已找到，无需下载");
                FileInfoDTO fileInfoDTO = new FileInfoDTO(fileMetadata.getId(), fileMetadata.getName(), fileMetadata.getType(), fileMetadata.getProjectId(), fileMetadata.getUpdateTime(), fileMetadata.getStorage(), fileMetadata.getPath(), FileUtils.fileToByte(file));
                fileInfoDTOList.add(fileInfoDTO);
            } else {
                LoggerUtil.info("文件【" + fileMetadata.getUpdateTime() + "_" + fileMetadata.getName() + "】在执行目录【" + fileMetadata.getProjectId() + "】未找到，需要下载");
                downloadFileRequest.add(this.genFileRequest(fileMetadata));
            }
        });
        List<FileInfoDTO> repositoryFileDTOList = fileManagerService.downloadFileBatch(downloadFileRequest);
        //将文件存储到执行文件目录中，避免多次执行时触发多次下载
        if (CollectionUtils.isNotEmpty(repositoryFileDTOList)) {
            repositoryFileDTOList.forEach(repositoryFile -> temporaryFileUtil.saveFileByParamCheck(repositoryFile.getProjectId(), repositoryFile.getId(), repositoryFile.getFileLastUpdateTime(), repositoryFile.getFileName(), repositoryFile.getType(), repositoryFile.getFileByte()));
            fileInfoDTOList.addAll(repositoryFileDTOList);
        }
        return fileInfoDTOList;
    }

    public void downloadByAttachmentBodyFileList(List<AttachmentBodyFile> downloadFileList) {
        LogUtil.info(JSON.toJSONString(downloadFileList) + " 获取执行文件开始");
        List<FileRequest> downloadFileRequest = new ArrayList<>();
        downloadFileList.forEach(attachmentBodyFile -> {
            FileRequest request = this.genFileRequest(attachmentBodyFile);
            downloadFileRequest.add(request);
        });

        List<FileInfoDTO> repositoryFileDTOList = fileManagerService.downloadFileBatch(downloadFileRequest);
        //将文件存储到执行文件目录中，避免多次执行时触发多次下载
        if (CollectionUtils.isNotEmpty(repositoryFileDTOList)) {
            repositoryFileDTOList.forEach(repositoryFile -> temporaryFileUtil.saveFileByParamCheck(repositoryFile.getProjectId(), repositoryFile.getId(), repositoryFile.getFileLastUpdateTime(), repositoryFile.getFileName(), repositoryFile.getType(), repositoryFile.getFileByte()));
        }
        LogUtil.info(JSON.toJSONString(downloadFileList) + " 获取执行文件结束");
    }

    public List<FileInfoDTO> downloadFileByIds(Collection<String> fileIdList) {
        if (CollectionUtils.isEmpty(fileIdList)) {
            return new ArrayList<>(0);
        }
        LogUtil.info(JSON.toJSONString(fileIdList) + " 获取文件开始");

        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(new ArrayList<>(fileIdList));
        List<FileMetadataWithBLOBs> fileMetadataWithBLOBs = fileMetadataMapper.selectByExampleWithBLOBs(example);

        List<FileRequest> requestList = new ArrayList<>();
        fileMetadataWithBLOBs.forEach(fileMetadata -> {
            requestList.add(this.genFileRequest(fileMetadata));
        });
        List<FileInfoDTO> repositoryFileDTOList = fileManagerService.downloadFileBatch(requestList);
        LogUtil.info(JSON.toJSONString(fileIdList) + " 获取文件结束。");
        return repositoryFileDTOList;
    }

    private FileRequest genFileRequest(AttachmentBodyFile attachmentBodyFile) {
        if (attachmentBodyFile != null) {
            FileRequest request = new FileRequest(attachmentBodyFile.getProjectId(), attachmentBodyFile.getName(), null);
            request.setResourceId(attachmentBodyFile.getFileMetadataId());
            request.setPath(attachmentBodyFile.getFilePath());
            request.setStorage(attachmentBodyFile.getFileStorage());
            request.setType(attachmentBodyFile.getFileType());
            request.setUpdateTime(attachmentBodyFile.getFileUpdateTime());
            if (StringUtils.equals(attachmentBodyFile.getFileStorage(), StorageConstants.GIT.name())) {
                try {
                    RemoteFileAttachInfo gitFileInfo = JSON.parseObject(attachmentBodyFile.getFileAttachInfoJson(), RemoteFileAttachInfo.class);
                    request.setFileAttachInfo(gitFileInfo);
                } catch (Exception e) {
                    LogUtil.error("解析Git附加信息【" + attachmentBodyFile.getFileAttachInfoJson() + "】失败!", e);
                }
            }
            return request;
        } else {
            return new FileRequest();
        }
    }

    private FileRequest genFileRequest(FileMetadataWithBLOBs fileMetadata) {
        if (fileMetadata != null) {
            FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
            request.setResourceId(fileMetadata.getId());
            request.setResourceType(fileMetadata.getResourceType());
            request.setPath(fileMetadata.getPath());
            request.setStorage(fileMetadata.getStorage());
            request.setUpdateTime(fileMetadata.getUpdateTime());
            if (StringUtils.equals(fileMetadata.getStorage(), StorageConstants.GIT.name())) {
                try {
                    RemoteFileAttachInfo gitFileInfo = JSON.parseObject(fileMetadata.getAttachInfo(), RemoteFileAttachInfo.class);
                    request.setFileAttachInfo(gitFileInfo);
                } catch (Exception e) {
                    LogUtil.error("解析Git附加信息【" + fileMetadata.getAttachInfo() + "】失败!", e);
                }
            }
            return request;
        } else {
            return new FileRequest();
        }
    }

    public FileMetadata pullFromRepository(FileMetadata request) {
        FileMetadataWithBLOBs baseMetadata = fileMetadataMapper.selectByPrimaryKey(request.getId());
        FileMetadata returnModel = baseMetadata;
        if (StringUtils.equals(baseMetadata.getStorage(), StorageConstants.GIT.name()) && StringUtils.isNotEmpty(baseMetadata.getAttachInfo())) {
            RemoteFileAttachInfo baseAttachInfo = JSON.parseObject(baseMetadata.getAttachInfo(), RemoteFileAttachInfo.class);
            FileModule fileModule = fileModuleService.get(baseMetadata.getModuleId());
            if (fileModule != null) {
                GitRepositoryUtil repositoryUtils = new GitRepositoryUtil(fileModule.getRepositoryPath(), fileModule.getRepositoryUserName(), fileModule.getRepositoryToken());
                RemoteFileAttachInfo gitFileAttachInfo = repositoryUtils.selectLastCommitIdByBranch(baseAttachInfo.getBranch(), baseAttachInfo.getFilePath());
                if (gitFileAttachInfo != null &&
                        !StringUtils.equals(gitFileAttachInfo.getCommitId(), baseAttachInfo.getCommitId())) {
                    //有新的commitId，更新fileMetadata的版本
                    long thisTime = System.currentTimeMillis();
                    FileMetadataWithBLOBs newMetadata = this.genOtherVersionFileMetadata(baseMetadata, thisTime, gitFileAttachInfo);
                    fileMetadataMapper.insert(newMetadata);

                    FileMetadataWithBLOBs updateOldData = new FileMetadataWithBLOBs();
                    updateOldData.setLatest(Boolean.FALSE);
                    FileMetadataExample example = new FileMetadataExample();
                    example.createCriteria().andIdEqualTo(baseMetadata.getId());
                    fileMetadataMapper.updateByExampleSelective(updateOldData, example);

                    returnModel = newMetadata;
                }
            }
        }
        return returnModel;
    }

    private FileMetadataWithBLOBs genOtherVersionFileMetadata(FileMetadataWithBLOBs baseMetadata, long operationTime, RemoteFileAttachInfo gitFileAttachInfo) {
        FileMetadataWithBLOBs newMetadata = new FileMetadataWithBLOBs();
        newMetadata.setDescription(baseMetadata.getDescription());
        newMetadata.setId(UUID.randomUUID().toString());
        newMetadata.setAttachInfo(JSON.toJSONString(gitFileAttachInfo));
        newMetadata.setName(baseMetadata.getName());
        newMetadata.setType(baseMetadata.getType());
        newMetadata.setSize(baseMetadata.getSize());
        newMetadata.setCreateTime(operationTime);
        newMetadata.setUpdateTime(operationTime);
        newMetadata.setStorage(baseMetadata.getStorage());
        newMetadata.setCreateUser(SessionUtils.getUserId());
        newMetadata.setProjectId(baseMetadata.getProjectId());
        newMetadata.setUpdateUser(SessionUtils.getUserId());
        newMetadata.setTags(baseMetadata.getTags());
        newMetadata.setLoadJar(baseMetadata.getLoadJar());
        newMetadata.setModuleId(baseMetadata.getModuleId());
        newMetadata.setPath(baseMetadata.getPath());
        newMetadata.setResourceType(baseMetadata.getResourceType());
        newMetadata.setRefId(baseMetadata.getRefId());
        newMetadata.setLatest(true);
        return newMetadata;
    }

    public List<FileMetadataWithBLOBs> getFileMetadataByIdList(List<String> fileMetadataIdList) {
        if (CollectionUtils.isNotEmpty(fileMetadataIdList)) {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andIdIn(fileMetadataIdList);
            return fileMetadataMapper.selectByExampleWithBLOBs(example);
        } else {
            return new ArrayList<>();
        }
    }

    public List<FileMetadataWithBLOBs> selectByIdAndType(List<String> idList, String jmx) {
        FileMetadataExample fileMetadataExample = new FileMetadataExample();
        fileMetadataExample.createCriteria().andIdIn(idList).andTypeEqualTo("JMX");
        return fileMetadataMapper.selectByExampleWithBLOBs(fileMetadataExample);
    }

    //检查项目下的文件，存在所属模块不存在的文件，将其挪到默认目录下
    public void checkProjectFileHasModuleId(String projectId) {
        List<String> illegalModuleFileIdList = requirementMapper.selectIllegalModuleIdListByProjectId(projectId);
        if (CollectionUtils.isNotEmpty(illegalModuleFileIdList)) {
            FileModule fileModule = fileModuleService.initDefaultNode(projectId);
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andIdIn(illegalModuleFileIdList);
            FileMetadataWithBLOBs updateModel = new FileMetadataWithBLOBs();
            updateModel.setModuleId(fileModule.getId());
            fileMetadataMapper.updateByExampleSelective(updateModel, example);
        }
    }

    public void deleteMetadataByIds(List<String> fileIds) {
        if (CollectionUtils.isNotEmpty(fileIds)) {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andIdIn(fileIds);
            fileMetadataMapper.deleteByExample(example);
        }
    }
}
