package com.apparelcert.service.impl;

import com.apparelcert.entity.ExamRecord;
import com.apparelcert.entity.Question;
import com.apparelcert.mapper.ExamRecordMapper;
import com.apparelcert.mapper.QuestionMapper;
import com.apparelcert.service.ExamService;
import com.apparelcert.service.CertificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线考试服务实现类
 */
@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private CertificationService certificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 考试时长（分钟）
    private static final int EXAM_DURATION = 60;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generatePaper(Long userId, Long applicationId, Long standardId, Integer questionCount) {
        Map<String, Object> result = new HashMap<>();

        // 从题库中随机抽取题目
        List<Question> allQuestions = questionMapper.findByStandardId(standardId);
        if (allQuestions == null || allQuestions.isEmpty()) {
            result.put("success", false);
            result.put("message", "题库中没有题目");
            return result;
        }

        // 随机抽取指定数量的题目
        Collections.shuffle(allQuestions);
        int count = Math.min(questionCount, allQuestions.size());
        List<Question> selectedQuestions = allQuestions.subList(0, count);

        // 计算总分
        int totalScore = selectedQuestions.stream().mapToInt(Question::getScore).sum();

        // 保存题目ID列表
        List<Long> questionIds = selectedQuestions.stream().map(Question::getId).collect(Collectors.toList());

        try {
            String paperContent = objectMapper.writeValueAsString(questionIds);

            // 创建考试记录
            ExamRecord examRecord = new ExamRecord();
            examRecord.setUserId(userId);
            examRecord.setApplicationId(applicationId);
            examRecord.setStandardId(standardId);
            examRecord.setPaperContent(paperContent);
            examRecord.setTotalScore(totalScore);
            examRecord.setDuration(EXAM_DURATION);
            examRecord.setStartTime(new Date());
            examRecord.setStatus(0); // 进行中
            examRecord.setCreateTime(new Date());
            examRecord.setUpdateTime(new Date());

            examRecordMapper.insert(examRecord);

            // 返回试卷信息（隐藏答案）
            List<Map<String, Object>> questionsWithoutAnswer = selectedQuestions.stream().map(q -> {
                Map<String, Object> qMap = new HashMap<>();
                qMap.put("id", q.getId());
                qMap.put("type", q.getType());
                qMap.put("content", q.getContent());
                qMap.put("options", q.getOptions());
                qMap.put("score", q.getScore());
                return qMap;
            }).collect(Collectors.toList());

            result.put("success", true);
            result.put("examRecordId", examRecord.getId());
            result.put("questions", questionsWithoutAnswer);
            result.put("totalScore", totalScore);
            result.put("duration", EXAM_DURATION);
            result.put("startTime", examRecord.getStartTime());

        } catch (JsonProcessingException e) {
            result.put("success", false);
            result.put("message", "生成试卷失败");
        }

        return result;
    }

    @Override
    public List<Question> getPaperQuestions(Long examRecordId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null) {
            return Collections.emptyList();
        }

        try {
            List<Long> questionIds = objectMapper.readValue(record.getPaperContent(), new TypeReference<List<Long>>() {});
            return questionIds.stream()
                    .map(questionMapper::selectById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitAnswers(Long examRecordId, Long userId, String answers) {
        Map<String, Object> result = new HashMap<>();

        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null) {
            result.put("success", false);
            result.put("message", "考试记录不存在");
            return result;
        }

        if (!record.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "无权提交此考试");
            return result;
        }

        if (record.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "考试已结束");
            return result;
        }

        // 检查是否超时
        if (checkTimeout(examRecordId)) {
            record.setStatus(2); // 超时
            record.setEndTime(new Date());
            record.setUpdateTime(new Date());
            examRecordMapper.updateById(record);
            result.put("success", false);
            result.put("message", "考试已超时");
            return result;
        }

        // 保存用户答案
        record.setUserAnswers(answers);
        record.setEndTime(new Date());
        record.setUpdateTime(new Date());

        // 自动评分
        int score = calculateScore(record, answers);
        record.setScore(score);
        record.setStatus(1); // 已完成

        examRecordMapper.updateById(record);

        // 更新认证申请的理论成绩
        certificationService.updateTheoryScore(record.getApplicationId(), score);

        result.put("success", true);
        result.put("score", score);
        result.put("totalScore", record.getTotalScore());
        result.put("passScore", record.getTotalScore() * 0.6); // 60分及格
        result.put("passed", score >= record.getTotalScore() * 0.6);

        return result;
    }

    @Override
    public int autoGrade(Long examRecordId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null || record.getUserAnswers() == null) {
            return 0;
        }
        return calculateScore(record, record.getUserAnswers());
    }

    private int calculateScore(ExamRecord record, String answersJson) {
        int score = 0;
        try {
            List<Long> questionIds = objectMapper.readValue(record.getPaperContent(), new TypeReference<List<Long>>() {});
            Map<String, String> userAnswers = objectMapper.readValue(answersJson, new TypeReference<Map<String, String>>() {});

            for (Long questionId : questionIds) {
                Question question = questionMapper.selectById(questionId);
                if (question != null) {
                    String userAnswer = userAnswers.get(String.valueOf(questionId));
                    if (userAnswer != null && userAnswer.equals(question.getAnswer())) {
                        score += question.getScore();
                    }
                }
            }
        } catch (JsonProcessingException e) {
            // 解析失败，返回0分
        }
        return score;
    }

    @Override
    public ExamRecord getExamRecord(Long examRecordId) {
        return examRecordMapper.selectById(examRecordId);
    }

    @Override
    public List<ExamRecord> getUserExamHistory(Long userId) {
        return examRecordMapper.findByUserId(userId);
    }

    @Override
    public boolean checkTimeout(Long examRecordId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null || record.getStartTime() == null) {
            return true;
        }

        long elapsedMinutes = (System.currentTimeMillis() - record.getStartTime().getTime()) / (1000 * 60);
        return elapsedMinutes > record.getDuration();
    }
}
