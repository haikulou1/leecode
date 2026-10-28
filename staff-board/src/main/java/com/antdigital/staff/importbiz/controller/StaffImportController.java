package com.antdigital.staff.importbiz.controller;

import com.antdigital.staff.common.model.ApiResponse;
import com.antdigital.staff.importbiz.service.ImportTemplateService;
import com.antdigital.staff.importbiz.service.StaffImportService;
import com.antdigital.staff.importbiz.vo.ImportTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 导入 oneapi 接口（W06-W08）与模板下载。
 */
@RestController
@RequestMapping("/api/staff/employee/import")
public class StaffImportController {

    private final StaffImportService importService;
    private final ImportTemplateService templateService;

    @Autowired
    public StaffImportController(StaffImportService importService,
                                 ImportTemplateService templateService) {
        this.importService = importService;
        this.templateService = templateService;
    }

    /** W06 批量导入员工。 */
    @PostMapping
    public ApiResponse<Map<String, Long>> submitImport(@RequestParam("file") MultipartFile file) throws IOException {
        Long taskId = importService.submitImport(file.getBytes(), file.getOriginalFilename(), null);
        Map<String, Long> data = new HashMap<>();
        data.put("taskId", taskId);
        return ApiResponse.success(data);
    }

    /** W07 下载导入模板。 */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] content = templateService.buildTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=staff_import_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    /** W08 查询导入任务结果。 */
    @GetMapping("/task/{taskId}")
    public ApiResponse<ImportTaskVO> getTaskResult(@PathVariable Long taskId) {
        return ApiResponse.success(importService.getTaskResult(taskId));
    }
}