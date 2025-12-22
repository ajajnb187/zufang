package org.example.rentingmanagement.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.Result;
import org.example.rentingmanagement.entity.SensitiveWord;
import org.example.rentingmanagement.mapper.SensitiveWordMapper;
import org.example.rentingmanagement.service.SensitiveWordService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 敏感词管理控制器
 * 供平台管理员管理敏感词库
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/sensitive-words")
@RequiredArgsConstructor
@SaCheckLogin
public class SensitiveWordController {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordService sensitiveWordService;

    /**
     * 获取敏感词列表（分页）
     */
    @GetMapping
    public Result<Map<String, Object>> getSensitiveWords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {

        Page<SensitiveWord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SensitiveWord::getWord, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(SensitiveWord::getCategory, category);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SensitiveWord::getStatus, status);
        }

        wrapper.orderByDesc(SensitiveWord::getCreatedAt);
        Page<SensitiveWord> result = sensitiveWordMapper.selectPage(page, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }

    /**
     * 添加敏感词
     */
    @PostMapping
    public Result<String> addSensitiveWord(@RequestBody SensitiveWord sensitiveWord) {
        // 检查是否已存在
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getWord, sensitiveWord.getWord());
        if (sensitiveWordMapper.selectCount(wrapper) > 0) {
            return Result.error("该敏感词已存在");
        }

        sensitiveWord.setStatus("active");
        sensitiveWord.setCreatedAt(LocalDateTime.now());
        sensitiveWord.setUpdatedAt(LocalDateTime.now());
        sensitiveWordMapper.insert(sensitiveWord);

        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();

        log.info("添加敏感词: {}", sensitiveWord.getWord());
        return Result.success("添加成功");
    }

    /**
     * 批量添加敏感词
     */
    @PostMapping("/batch")
    public Result<String> batchAddSensitiveWords(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> words = (List<String>) body.get("words");
        String category = (String) body.getOrDefault("category", "default");
        Integer level = (Integer) body.getOrDefault("level", 1);

        int addCount = 0;
        for (String word : words) {
            if (word == null || word.trim().isEmpty()) continue;

            LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SensitiveWord::getWord, word.trim());
            if (sensitiveWordMapper.selectCount(wrapper) == 0) {
                SensitiveWord sensitiveWord = new SensitiveWord();
                sensitiveWord.setWord(word.trim());
                sensitiveWord.setCategory(category);
                sensitiveWord.setLevel(level);
                sensitiveWord.setStatus("active");
                sensitiveWord.setCreatedAt(LocalDateTime.now());
                sensitiveWord.setUpdatedAt(LocalDateTime.now());
                sensitiveWordMapper.insert(sensitiveWord);
                addCount++;
            }
        }

        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();

        log.info("批量添加敏感词: {}个", addCount);
        return Result.success("成功添加" + addCount + "个敏感词");
    }

    /**
     * 更新敏感词
     */
    @PutMapping("/{wordId}")
    public Result<String> updateSensitiveWord(@PathVariable Long wordId, @RequestBody SensitiveWord sensitiveWord) {
        SensitiveWord existing = sensitiveWordMapper.selectById(wordId);
        if (existing == null) {
            return Result.error("敏感词不存在");
        }

        existing.setWord(sensitiveWord.getWord());
        existing.setCategory(sensitiveWord.getCategory());
        existing.setLevel(sensitiveWord.getLevel());
        existing.setUpdatedAt(LocalDateTime.now());
        sensitiveWordMapper.updateById(existing);

        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();

        log.info("更新敏感词: {}", existing.getWord());
        return Result.success("更新成功");
    }

    /**
     * 删除敏感词
     */
    @DeleteMapping("/{wordId}")
    public Result<String> deleteSensitiveWord(@PathVariable Long wordId) {
        SensitiveWord existing = sensitiveWordMapper.selectById(wordId);
        if (existing == null) {
            return Result.error("敏感词不存在");
        }

        sensitiveWordMapper.deleteById(wordId);

        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();

        log.info("删除敏感词: {}", existing.getWord());
        return Result.success("删除成功");
    }

    /**
     * 切换敏感词状态
     */
    @PutMapping("/{wordId}/toggle")
    public Result<String> toggleStatus(@PathVariable Long wordId) {
        SensitiveWord existing = sensitiveWordMapper.selectById(wordId);
        if (existing == null) {
            return Result.error("敏感词不存在");
        }

        existing.setStatus("active".equals(existing.getStatus()) ? "inactive" : "active");
        existing.setUpdatedAt(LocalDateTime.now());
        sensitiveWordMapper.updateById(existing);

        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();

        return Result.success(existing.getStatus().equals("active") ? "已启用" : "已禁用");
    }

    /**
     * 测试敏感词过滤
     */
    @PostMapping("/test")
    public Result<Map<String, Object>> testFilter(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isEmpty()) {
            return Result.error("内容不能为空");
        }

        SensitiveWordService.FilterResult result = sensitiveWordService.filterContent(content);

        Map<String, Object> data = new HashMap<>();
        data.put("originalContent", content);
        data.put("filteredContent", result.getFilteredContent());
        data.put("hasSensitive", result.isHasSensitive());
        data.put("sensitiveWords", result.getSensitiveWords());

        return Result.success(data);
    }

    /**
     * 重新加载敏感词库
     */
    @PostMapping("/reload")
    public Result<String> reloadSensitiveWords() {
        sensitiveWordService.reloadSensitiveWords();
        return Result.success("敏感词库已重新加载");
    }

    /**
     * 获取敏感词分类统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> data = new HashMap<>();

        // 总数
        long total = sensitiveWordMapper.selectCount(null);
        data.put("total", total);

        // 启用数量
        LambdaQueryWrapper<SensitiveWord> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(SensitiveWord::getStatus, "active");
        long activeCount = sensitiveWordMapper.selectCount(activeWrapper);
        data.put("activeCount", activeCount);

        // 按分类统计
        List<Map<String, Object>> categoryStats = sensitiveWordMapper.selectMaps(
                new LambdaQueryWrapper<SensitiveWord>()
                        .select(SensitiveWord::getCategory)
                        .groupBy(SensitiveWord::getCategory)
        );
        data.put("categories", categoryStats);

        return Result.success(data);
    }
}
