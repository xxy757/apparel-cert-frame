package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.ExamRecord;
import com.apparelcert.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 在线考试控制器
 */
@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    /**
     * 生成试卷
     * @param params 包含userId, applicationId, standardId, questionCount
     * @return 试卷信息
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generatePaper(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long applicationId = Long.valueOf(params.get("applicationId").toString());
        Long standardId = Long.valueOf(params.get("standardId").toString());
        Integer questionCount = params.get("questionCount") != null 
                ? Integer.valueOf(params.get("questionCount").toString()) 
                : 20;

        Map<String, Object> result = examService.generatePaper(userId, applicationId, standardId, questionCount);
        Boolean success = (Boolean) result.get("success");

        if (success) {
            return Result.success(result);
        } else {
            return Result.error((String) result.get("message"));
        }
    }

    /**
     * 提交答案
     * @param params 包含examRecordId, userId, answers
     * @return 考试结果
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitAnswers(@RequestBody Map<String, Object> params) {
        Long examRecordId = Long.valueOf(params.get("examRecordId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        String answers = params.get("answers").toString();

        Map<String, Object> result = examService.submitAnswers(examRecordId, userId, answers);
        Boolean success = (Boolean) result.get("success");

        if (success) {
            return Result.success(result);
        } else {
            return Result.error((String) result.get("message"));
        }
    }

    /**
     * 获取考试记录
     * @param examRecordId 考试记录ID
     * @return 考试记录
     */
    @GetMapping("/record/{examRecordId}")
    public Result<ExamRecord> getExamRecord(@PathVariable Long examRecordId) {
        ExamRecord record = examService.getExamRecord(examRecordId);
        if (record != null) {
            return Result.success(record);
        } else {
            return Result.error("考试记录不存在");
        }
    }

    /**
     * 获取用户考试历史
     * @param userId 用户ID
     * @return 考试记录列表
     */
    @GetMapping("/history/{userId}")
    public Result<List<ExamRecord>> getUserExamHistory(@PathVariable Long userId) {
        List<ExamRecord> history = examService.getUserExamHistory(userId);
        return Result.success(history);
    }

    /**
     * 检查考试是否超时
     * @param examRecordId 考试记录ID
     * @return 是否超时
     */
    @GetMapping("/check-timeout/{examRecordId}")
    public Result<Boolean> checkTimeout(@PathVariable Long examRecordId) {
        boolean timeout = examService.checkTimeout(examRecordId);
        return Result.success(timeout);
    }
}
