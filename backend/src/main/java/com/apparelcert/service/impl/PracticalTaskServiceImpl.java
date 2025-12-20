package com.apparelcert.service.impl;

import com.apparelcert.entity.PracticalTask;
import com.apparelcert.mapper.PracticalTaskMapper;
import com.apparelcert.service.PracticalTaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 实操任务服务实现类
 */
@Service
public class PracticalTaskServiceImpl extends ServiceImpl<PracticalTaskMapper, PracticalTask> implements PracticalTaskService {

    @Autowired
    private PracticalTaskMapper taskMapper;

    @Override
    public Page<PracticalTask> pageQuery(Integer page, Integer size, String keyword, String type, Integer level) {
        Page<PracticalTask> pageInfo = new Page<>(page, size);
        QueryWrapper<PracticalTask> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword).or().like("description", keyword);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (level != null) {
            wrapper.eq("level", level);
        }
        
        wrapper.orderByAsc("type");
        wrapper.orderByAsc("level");
        return taskMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public PracticalTask getById(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    public boolean saveOrUpdateTask(PracticalTask task) {
        return this.saveOrUpdate(task);
    }

    @Override
    public boolean deleteTask(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean deleteBatchTasks(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public PracticalTask getByCertificationAndLevel(String type, Integer level) {
        QueryWrapper<PracticalTask> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        wrapper.eq("level", level);
        return taskMapper.selectOne(wrapper);
    }

    @Override
    public List<String> getAllTypes() {
        // TODO: 实现获取所有实操任务类型的逻辑
        return null;
    }

    @Override
    public List<PracticalTask> getByType(String type) {
        QueryWrapper<PracticalTask> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        wrapper.orderByAsc("level");
        return taskMapper.selectList(wrapper);
    }
}