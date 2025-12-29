package com.apparelcert.service.impl;

import com.apparelcert.entity.ResumeCollection;
import com.apparelcert.mapper.ResumeCollectionMapper;
import com.apparelcert.service.ResumeCollectionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 简历收藏服务实现类
 */
@Service
public class ResumeCollectionServiceImpl extends ServiceImpl<ResumeCollectionMapper, ResumeCollection> 
        implements ResumeCollectionService {

    @Autowired
    private ResumeCollectionMapper collectionMapper;

    @Override
    public boolean collectResume(Long enterpriseId, Long resumeUserId) {
        // 检查是否已收藏
        if (isCollected(enterpriseId, resumeUserId)) {
            return true;
        }
        
        ResumeCollection collection = new ResumeCollection();
        collection.setEnterpriseId(enterpriseId);
        collection.setResumeUserId(resumeUserId);
        collection.setIntentionLevel(2); // 默认中等意向
        collection.setCollectTime(new Date());
        
        return this.save(collection);
    }

    @Override
    public boolean uncollectResume(Long enterpriseId, Long resumeUserId) {
        QueryWrapper<ResumeCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("enterprise_id", enterpriseId);
        wrapper.eq("resume_user_id", resumeUserId);
        return this.remove(wrapper);
    }

    @Override
    public boolean isCollected(Long enterpriseId, Long resumeUserId) {
        QueryWrapper<ResumeCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("enterprise_id", enterpriseId);
        wrapper.eq("resume_user_id", resumeUserId);
        return this.count(wrapper) > 0;
    }

    @Override
    public boolean updateIntentionLevel(Long enterpriseId, Long resumeUserId, Integer intentionLevel) {
        ResumeCollection collection = getCollectionDetail(enterpriseId, resumeUserId);
        if (collection == null) {
            return false;
        }
        collection.setIntentionLevel(intentionLevel);
        return this.updateById(collection);
    }

    @Override
    public boolean updateNotes(Long enterpriseId, Long resumeUserId, String notes) {
        ResumeCollection collection = getCollectionDetail(enterpriseId, resumeUserId);
        if (collection == null) {
            return false;
        }
        collection.setNotes(notes);
        return this.updateById(collection);
    }

    @Override
    public boolean updateTags(Long enterpriseId, Long resumeUserId, String tags) {
        ResumeCollection collection = getCollectionDetail(enterpriseId, resumeUserId);
        if (collection == null) {
            return false;
        }
        collection.setTags(tags);
        return this.updateById(collection);
    }

    @Override
    public Page<ResumeCollection> getCollectedResumes(Long enterpriseId, Integer page, Integer size, Integer intentionLevel) {
        Page<ResumeCollection> pageInfo = new Page<>(page, size);
        QueryWrapper<ResumeCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("enterprise_id", enterpriseId);
        if (intentionLevel != null) {
            wrapper.eq("intention_level", intentionLevel);
        }
        wrapper.orderByDesc("collect_time");
        return collectionMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public ResumeCollection getCollectionDetail(Long enterpriseId, Long resumeUserId) {
        QueryWrapper<ResumeCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("enterprise_id", enterpriseId);
        wrapper.eq("resume_user_id", resumeUserId);
        return collectionMapper.selectOne(wrapper);
    }

    @Override
    public int batchCollect(Long enterpriseId, List<Long> resumeUserIds) {
        int count = 0;
        for (Long resumeUserId : resumeUserIds) {
            if (collectResume(enterpriseId, resumeUserId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int batchUncollect(Long enterpriseId, List<Long> resumeUserIds) {
        int count = 0;
        for (Long resumeUserId : resumeUserIds) {
            if (uncollectResume(enterpriseId, resumeUserId)) {
                count++;
            }
        }
        return count;
    }
}
