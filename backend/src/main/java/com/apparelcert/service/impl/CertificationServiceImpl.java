package com.apparelcert.service.impl;

import com.apparelcert.entity.Certification;
import com.apparelcert.mapper.CertificationMapper;
import com.apparelcert.service.CertificationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 技能认证服务实现类
 */
@Service
public class CertificationServiceImpl extends ServiceImpl<CertificationMapper, Certification> implements CertificationService {

    @Autowired
    private CertificationMapper certificationMapper;

    @Override
    public boolean applyCertification(Certification certification) {
        certification.setApplyTime(new Date());
        certification.setStatus(0); // 0: 待审核
        return this.save(certification);
    }

    @Override
    public List<Certification> getByUserId(Long userId) {
        QueryWrapper<Certification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("apply_time");
        return certificationMapper.selectList(wrapper);
    }

    @Override
    public Certification getById(Long id) {
        return certificationMapper.selectById(id);
    }

    @Override
    public boolean uploadPractical(Long certificationId, String fileUrl) {
        Certification certification = new Certification();
        certification.setId(certificationId);
        certification.setStatus(1); // 1: 实操已提交
        // TODO: 保存实操成果URL
        return this.updateById(certification);
    }

    @Override
    public Integer getProgress(Long certificationId) {
        Certification certification = this.getById(certificationId);
        if (certification == null) {
            return 0;
        }
        // 根据状态返回进度：0-待审核(20%), 1-实操已提交(50%), 2-理论考试已完成(70%), 3-已通过(100%), 4-未通过(0%)
        switch (certification.getStatus()) {
            case 0:
                return 20;
            case 1:
                return 50;
            case 2:
                return 70;
            case 3:
                return 100;
            default:
                return 0;
        }
    }

    @Override
    public Page<Certification> pageQuery(Integer page, Integer size, Long userId) {
        Page<Certification> pageInfo = new Page<>(page, size);
        QueryWrapper<Certification> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        wrapper.orderByDesc("apply_time");
        return certificationMapper.selectPage(pageInfo, wrapper);
    }
}