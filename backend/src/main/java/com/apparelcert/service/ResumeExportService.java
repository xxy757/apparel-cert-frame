package com.apparelcert.service;

import java.util.List;

/**
 * 简历导出服务接口
 */
public interface ResumeExportService {
    
    /**
     * 批量导出简历为Excel
     * @param resumeIds 简历ID列表
     * @return Excel文件路径
     */
    String batchExportToExcel(List<Long> resumeIds);
    
    /**
     * 批量导出简历为PDF（打包成ZIP）
     * @param resumeIds 简历ID列表
     * @return ZIP文件路径
     */
    String batchExportToPdf(List<Long> resumeIds);
}
