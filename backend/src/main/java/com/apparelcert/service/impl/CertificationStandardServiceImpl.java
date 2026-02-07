package com.apparelcert.service.impl;

import com.apparelcert.entity.CertificationStandard;
import com.apparelcert.mapper.CertificationStandardMapper;
import com.apparelcert.service.CertificationStandardService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证标准服务实现类
 */
@Service
public class CertificationStandardServiceImpl extends ServiceImpl<CertificationStandardMapper, CertificationStandard> implements CertificationStandardService {

    @Autowired
    private CertificationStandardMapper standardMapper;

    @Override
    public Page<CertificationStandard> pageQuery(Integer page, Integer size, String keyword, String type, Integer level) {
        Page<CertificationStandard> pageInfo = new Page<>(page, size);
        QueryWrapper<CertificationStandard> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword).or().like("description", keyword);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("job_type", type);
        }
        if (level != null) {
            wrapper.eq("level", level);
        }
        
        wrapper.orderByAsc("job_type");
        wrapper.orderByAsc("level");
        return standardMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public CertificationStandard getById(Long id) {
        return standardMapper.selectById(id);
    }

    @Override
    public boolean saveOrUpdateStandard(CertificationStandard standard) {
        return this.saveOrUpdate(standard);
    }

    @Override
    public boolean deleteStandard(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<String> getAllTypes() {
        // TODO: 实现获取所有认证标准类型的逻辑
        return null;
    }

    @Override
    public List<CertificationStandard> getByType(String type) {
        QueryWrapper<CertificationStandard> wrapper = new QueryWrapper<>();
        wrapper.eq("job_type", type);
        wrapper.orderByAsc("level");
        return standardMapper.selectList(wrapper);
    }

    @Override
    public CertificationStandard getByTypeAndLevel(String type, Integer level) {
        QueryWrapper<CertificationStandard> wrapper = new QueryWrapper<>();
        wrapper.eq("job_type", type);
        wrapper.eq("level", level);
        return standardMapper.selectOne(wrapper);
    }
}
