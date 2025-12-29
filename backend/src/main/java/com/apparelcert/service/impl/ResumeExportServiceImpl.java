package com.apparelcert.service.impl;

import com.apparelcert.entity.Resume;
import com.apparelcert.mapper.ResumeMapper;
import com.apparelcert.service.ResumeExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 简历导出服务实现类
 */
@Service
public class ResumeExportServiceImpl implements ResumeExportService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeExportServiceImpl.class);

    @Autowired
    private ResumeMapper resumeMapper;
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String batchExportToExcel(List<Long> resumeIds) {
        try {
            // 查询简历数据
            List<Resume> resumes = resumeMapper.selectBatchIds(resumeIds);
            if (resumes.isEmpty()) {
                return null;
            }
            
            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("简历列表");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"姓名", "手机号", "邮箱", "求职意向", "工作经验", "最高学历", "技能", "更新时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (Resume resume : resumes) {
                Row row = sheet.createRow(rowNum++);
                
                // 解析JSON数据
                Map<String, Object> basicInfo = parseJson(resume.getBasicInfo());
                List<Map<String, Object>> skills = parseJsonArray(resume.getSkills());
                
                row.createCell(0).setCellValue(getStringValue(basicInfo, "name"));
                row.createCell(1).setCellValue(getStringValue(basicInfo, "phone"));
                row.createCell(2).setCellValue(getStringValue(basicInfo, "email"));
                row.createCell(3).setCellValue(getStringValue(basicInfo, "jobIntention"));
                row.createCell(4).setCellValue(getStringValue(basicInfo, "workYears") + "年");
                row.createCell(5).setCellValue(getStringValue(basicInfo, "education"));
                row.createCell(6).setCellValue(formatSkills(skills));
                row.createCell(7).setCellValue(resume.getUpdateTime() != null ? sdf.format(resume.getUpdateTime()) : "");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 保存文件
            String subDir = "exports/" + new SimpleDateFormat("yyyyMMdd").format(new Date());
            Path exportDir = Paths.get(uploadPath, subDir);
            Files.createDirectories(exportDir);
            
            String filename = "resumes_" + System.currentTimeMillis() + ".xlsx";
            Path filePath = exportDir.resolve(filename);
            
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
            workbook.close();
            
            String exportUrl = "/uploads/" + subDir + "/" + filename;
            logger.info("批量导出Excel成功: {}", exportUrl);
            return exportUrl;
            
        } catch (Exception e) {
            logger.error("批量导出Excel失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String batchExportToPdf(List<Long> resumeIds) {
        try {
            // 查询简历数据
            List<Resume> resumes = resumeMapper.selectBatchIds(resumeIds);
            if (resumes.isEmpty()) {
                return null;
            }
            
            // 创建临时目录存放PDF
            String subDir = "exports/" + new SimpleDateFormat("yyyyMMdd").format(new Date());
            Path exportDir = Paths.get(uploadPath, subDir);
            Files.createDirectories(exportDir);
            
            // 生成每个简历的PDF
            List<Path> pdfFiles = new ArrayList<>();
            for (Resume resume : resumes) {
                Path pdfPath = generateResumePdf(resume, exportDir);
                if (pdfPath != null) {
                    pdfFiles.add(pdfPath);
                }
            }
            
            if (pdfFiles.isEmpty()) {
                return null;
            }
            
            // 打包成ZIP
            String zipFilename = "resumes_" + System.currentTimeMillis() + ".zip";
            Path zipPath = exportDir.resolve(zipFilename);
            
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                for (Path pdfFile : pdfFiles) {
                    ZipEntry entry = new ZipEntry(pdfFile.getFileName().toString());
                    zos.putNextEntry(entry);
                    Files.copy(pdfFile, zos);
                    zos.closeEntry();
                    
                    // 删除临时PDF文件
                    Files.deleteIfExists(pdfFile);
                }
            }
            
            String exportUrl = "/uploads/" + subDir + "/" + zipFilename;
            logger.info("批量导出PDF成功: {}", exportUrl);
            return exportUrl;
            
        } catch (Exception e) {
            logger.error("批量导出PDF失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 生成单个简历PDF
     */
    private Path generateResumePdf(Resume resume, Path exportDir) {
        try {
            Map<String, Object> basicInfo = parseJson(resume.getBasicInfo());
            String name = getStringValue(basicInfo, "name");
            if (name.isEmpty()) {
                name = "resume_" + resume.getId();
            }
            
            String filename = name + "_" + resume.getId() + ".pdf";
            Path pdfPath = exportDir.resolve(filename);
            
            // 生成简单PDF内容
            String pdfContent = buildResumePdfContent(resume, basicInfo);
            
            try (FileOutputStream fos = new FileOutputStream(pdfPath.toFile())) {
                fos.write(pdfContent.getBytes(StandardCharsets.UTF_8));
            }
            
            return pdfPath;
        } catch (Exception e) {
            logger.error("生成简历PDF失败: resumeId={}, error={}", resume.getId(), e.getMessage());
            return null;
        }
    }
    
    /**
     * 构建简历PDF内容
     */
    private String buildResumePdfContent(Resume resume, Map<String, Object> basicInfo) {
        StringBuilder content = new StringBuilder();
        content.append("%PDF-1.4\n");
        content.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        content.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        content.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");
        
        StringBuilder text = new StringBuilder();
        text.append("BT\n");
        text.append("/F1 24 Tf\n50 780 Td\n(Resume) Tj\n");
        text.append("/F1 14 Tf\n50 740 Td\n(Name: ").append(escapeForPdf(getStringValue(basicInfo, "name"))).append(") Tj\n");
        text.append("/F1 12 Tf\n50 710 Td\n(Phone: ").append(escapeForPdf(getStringValue(basicInfo, "phone"))).append(") Tj\n");
        text.append("/F1 12 Tf\n50 680 Td\n(Email: ").append(escapeForPdf(getStringValue(basicInfo, "email"))).append(") Tj\n");
        text.append("/F1 12 Tf\n50 650 Td\n(Job Intention: ").append(escapeForPdf(getStringValue(basicInfo, "jobIntention"))).append(") Tj\n");
        text.append("ET\n");
        
        String streamContent = text.toString();
        content.append("4 0 obj\n<< /Length ").append(streamContent.length()).append(" >>\nstream\n");
        content.append(streamContent);
        content.append("endstream\nendobj\n");
        content.append("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        content.append("xref\n0 6\n0000000000 65535 f \n");
        content.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n0\n%%EOF");
        
        return content.toString();
    }
    
    private String escapeForPdf(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replaceAll("[^\\x00-\\x7F]", "?");
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, Map.class);
            }
        } catch (Exception e) {
            logger.error("JSON解析失败: {}", e.getMessage());
        }
        return new HashMap<>();
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseJsonArray(String json) {
        try {
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, List.class);
            }
        } catch (Exception e) {
            logger.error("JSON数组解析失败: {}", e.getMessage());
        }
        return new ArrayList<>();
    }
    
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
    
    private String formatSkills(List<Map<String, Object>> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> skill : skills) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(getStringValue(skill, "name"));
        }
        return sb.toString();
    }
}
