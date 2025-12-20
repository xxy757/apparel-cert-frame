package com.apparelcert.service;

import com.apparelcert.entity.Certificate;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 证书服务接口
 */
public interface CertificateService extends IService<Certificate> {
    /**
     * 分页查询证书列表
     */
    Page<Certificate> pageQuery(Integer page, Integer size, String keyword, String certificationType, Integer level, String status);
    
    /**
     * 获取证书详情
     */
    Certificate getById(Long id);
    
    /**
     * 保存或更新证书
     */
    boolean saveOrUpdateCertificate(Certificate certificate);
    
    /**
     * 删除证书
     */
    boolean deleteCertificate(Long id);
    
    /**
     * 批量删除证书
     */
    boolean deleteBatchCertificates(List<Long> ids);
    
    /**
     * 生成证书
     */
    Certificate generateCertificate(Long certificationId);
    
    /**
     * 验证证书真实性
     */
    boolean verifyCertificate(String certificateNumber);
    
    /**
     * 根据证书编号获取证书
     */
    Certificate getByCertificateNumber(String certificateNumber);
    
    /**
     * 获取用户的所有证书
     */
    List<Certificate> getByUserId(Long userId);
    
    /**
     * 更新证书状态
     */
    boolean updateCertificateStatus(Long certificateId, String status);
    
    /**
     * 导出证书为PDF
     */
    String exportCertificateToPdf(Long certificateId);
}