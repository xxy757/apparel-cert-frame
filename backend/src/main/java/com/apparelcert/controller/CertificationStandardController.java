package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.CertificationStandard;
import com.apparelcert.service.CertificationStandardService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证标准控制器
 */
@RestController
@RequestMapping("/api/admin/certification/standard")
public class CertificationStandardController {

    @Autowired
    private CertificationStandardService standardService;

    /**
     * 分页查询认证标准列表
     */
    @GetMapping
    public Result<Page<CertificationStandard>> getStandardList(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer level) {
        Page<CertificationStandard> pageInfo = standardService.pageQuery(page, size, keyword, type, level);
        return Result.success(pageInfo);
    }

    /**
     * 获取认证标准详情
     */
    @GetMapping("/detail")
    public Result<CertificationStandard> getStandardDetail(@RequestParam Long standardId) {
        CertificationStandard standard = standardService.getById(standardId);
        return Result.success(standard);
    }

    /**
     * 保存或更新认证标准
     */
    @PostMapping
    public Result<Boolean> saveOrUpdateStandard(@RequestBody CertificationStandard standard) {
        boolean result = standardService.saveOrUpdateStandard(standard);
        return Result.success(result);
    }

    /**
     * 删除认证标准
     */
    @DeleteMapping
    public Result<Boolean> deleteStandard(@RequestParam Long standardId) {
        boolean result = standardService.deleteStandard(standardId);
        return Result.success(result);
    }

    /**
     * 获取所有认证标准类型
     */
    @GetMapping("/types")
    public Result<List<String>> getAllTypes() {
        List<String> types = standardService.getAllTypes();
        return Result.success(types);
    }

    /**
     * 根据类型获取认证标准
     */
    @GetMapping("/by-type")
    public Result<List<CertificationStandard>> getByType(@RequestParam String type) {
        List<CertificationStandard> standards = standardService.getByType(type);
        return Result.success(standards);
    }

    /**
     * 根据类型和等级获取认证标准
     */
    @GetMapping("/by-type-level")
    public Result<CertificationStandard> getByTypeAndLevel(
            @RequestParam String type,
            @RequestParam Integer level) {
        CertificationStandard standard = standardService.getByTypeAndLevel(type, level);
        return Result.success(standard);
    }
}