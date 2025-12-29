package com.apparelcert.service;

import com.apparelcert.entity.ExamRecord;
import com.apparelcert.entity.Question;

import java.util.List;
import java.util.Map;

/**
 * 在线考试服务接口
 */
public interface ExamService {

    /**
     * 生成试卷
     * @param userId 用户ID
     * @param applicationId 认证申请ID
     * @param standardId 认证标准ID
     * @param questionCount 题目数量
     * @return 试卷信息，包含考试记录ID和题目列表
     */
    Map<String, Object> generatePaper(Long userId, Long applicationId, Long standardId, Integer questionCount);

    /**
     * 获取试卷题目
     * @param examRecordId 考试记录ID
     * @return 题目列表（不含答案）
     */
    List<Question> getPaperQuestions(Long examRecordId);

    /**
     * 提交答案
     * @param examRecordId 考试记录ID
     * @param userId 用户ID
     * @param answers 用户答案（JSON格式）
     * @return 考试结果，包含得分和详情
     */
    Map<String, Object> submitAnswers(Long examRecordId, Long userId, String answers);

    /**
     * 自动评分
     * @param examRecordId 考试记录ID
     * @return 得分
     */
    int autoGrade(Long examRecordId);

    /**
     * 获取考试记录
     * @param examRecordId 考试记录ID
     * @return 考试记录
     */
    ExamRecord getExamRecord(Long examRecordId);

    /**
     * 获取用户的考试历史
     * @param userId 用户ID
     * @return 考试记录列表
     */
    List<ExamRecord> getUserExamHistory(Long userId);

    /**
     * 检查考试是否超时
     * @param examRecordId 考试记录ID
     * @return 是否超时
     */
    boolean checkTimeout(Long examRecordId);
}
