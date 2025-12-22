package org.example.rentingmanagement.service.impl;

import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.mapper.SensitiveWordMapper;
import org.example.rentingmanagement.service.SensitiveWordService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词过滤服务实现类
 * 基于DFA算法实现高性能敏感词过滤
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;

    private SensitiveWordBs sensitiveWordBs;

    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        log.info("初始化敏感词库...");
        reloadSensitiveWords();
        log.info("敏感词库初始化完成");
    }

    @Override
    public void reloadSensitiveWords() {
        try {
            // 从数据库加载敏感词
            List<String> denyWords = sensitiveWordMapper.findAllActiveWords();
            List<String> allowWords = sensitiveWordMapper.findAllWhitelistWords();

            log.info("加载敏感词: {}个, 白名单词: {}个", denyWords.size(), allowWords.size());

            // 自定义敏感词加载
            IWordDeny customDeny = () -> denyWords;
            IWordDeny defaultDeny = WordDenys.defaults();

            // 自定义白名单加载
            IWordAllow customAllow = () -> allowWords;
            IWordAllow defaultAllow = WordAllows.defaults();

            // 构建敏感词过滤器：系统词库 + 自定义词库
            sensitiveWordBs = SensitiveWordBs.newInstance()
                    .wordDeny(WordDenys.chains(defaultDeny, customDeny))
                    .wordAllow(WordAllows.chains(defaultAllow, customAllow))
                    .ignoreCase(true)       // 忽略大小写
                    .ignoreWidth(true)      // 忽略全角半角
                    .ignoreNumStyle(true)   // 忽略数字样式
                    .ignoreChineseStyle(true) // 忽略中文样式
                    .ignoreEnglishStyle(true) // 忽略英文样式
                    .ignoreRepeat(true)     // 忽略重复字符
                    .enableWordCheck(true)  // 启用单词检测
                    .init();

            log.info("敏感词过滤器初始化成功");
        } catch (Exception e) {
            log.error("敏感词库加载失败，使用系统默认词库: {}", e.getMessage());
            // 如果数据库加载失败，使用系统默认词库
            sensitiveWordBs = SensitiveWordBs.newInstance()
                    .wordDeny(WordDenys.defaults())
                    .wordAllow(WordAllows.defaults())
                    .ignoreCase(true)
                    .ignoreWidth(true)
                    .init();
        }
    }

    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return sensitiveWordBs.contains(text);
    }

    @Override
    public List<String> findAllSensitiveWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return sensitiveWordBs.findAll(text);
    }

    @Override
    public String findFirstSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return sensitiveWordBs.findFirst(text);
    }

    @Override
    public String replaceSensitiveWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        return sensitiveWordBs.replace(text);
    }

    @Override
    public String replaceSensitiveWords(String text, char replacement) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        // 先获取所有敏感词，然后手动替换
        List<String> sensitiveWords = sensitiveWordBs.findAll(text);
        if (sensitiveWords.isEmpty()) {
            return text;
        }
        String result = text;
        for (String word : sensitiveWords) {
            String replaceStr = String.valueOf(replacement).repeat(word.length());
            result = result.replace(word, replaceStr);
        }
        return result;
    }

    @Override
    public FilterResult filterContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new FilterResult(content, false, new ArrayList<>());
        }

        // 查找所有敏感词
        List<String> sensitiveWords = sensitiveWordBs.findAll(content);
        boolean hasSensitive = !sensitiveWords.isEmpty();

        // 替换敏感词为*
        String filteredContent = hasSensitive ? sensitiveWordBs.replace(content) : content;

        if (hasSensitive) {
            log.debug("检测到敏感词: {}", sensitiveWords);
        }

        return new FilterResult(filteredContent, hasSensitive, sensitiveWords);
    }

    @Override
    public void addSensitiveWord(String word) {
        // 动态添加敏感词需要重新初始化
        log.info("添加敏感词: {}", word);
        reloadSensitiveWords();
    }

    @Override
    public void removeSensitiveWord(String word) {
        // 动态移除敏感词需要重新初始化
        log.info("移除敏感词: {}", word);
        reloadSensitiveWords();
    }
}
