package com.apparelcert.service;

import com.apparelcert.entity.Certificate;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

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
    
    /**
     * 检查证书有效期
     * @param certificateId 证书ID
     * @return 有效期信息（剩余天数、是否即将过期、是否已过期）
     */
    Map<String, Object> checkCertificateValidity(Long certificateId);
    
    /**
     * 获取即将过期的证书列表
     * @param userId 用户ID
     * @param daysBeforeExpire 过期前天数
     * @return 即将过期的证书列表
     */
    List<Certificate> getExpiringCertificates(Long userId, Integer daysBeforeExpire);
    
    /**
     * 续期证书
     * @param certificateId 证书ID
     * @param years 续期年数
     * @return 是否成功
     */
    boolean renewCertificate(Long certificateId, Integer years);
    
    /**
     * 生成证书分享链接
     * @param certificateId 证书ID
     * @return 分享链接
     */
    String generateShareLink(Long certificateId);
    
    /**
     * 通过分享码获取证书信息
     * @param shareCode 分享码
     * @return 证书信息
     */
    Map<String, Object> getCertificateByShareCode(String shareCode);
}