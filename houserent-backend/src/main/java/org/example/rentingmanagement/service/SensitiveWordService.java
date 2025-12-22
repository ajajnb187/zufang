package org.example.rentingmanagement.service;

import java.util.List;

/**
 * 敏感词过滤服务接口
 */
public interface SensitiveWordService {

    /**
     * 检查文本是否包含敏感词
     * @param text 待检查文本
     * @return 是否包含敏感词
     */
    boolean containsSensitiveWord(String text);

    /**
     * 获取文本中所有敏感词
     * @param text 待检查文本
     * @return 敏感词列表
     */
    List<String> findAllSensitiveWords(String text);

    /**
     * 获取文本中第一个敏感词
     * @param text 待检查文本
     * @return 第一个敏感词，没有则返回null
     */
    String findFirstSensitiveWord(String text);

    /**
     * 替换文本中的敏感词为*
     * @param text 待处理文本
     * @return 过滤后的文本
     */
    String replaceSensitiveWords(String text);

    /**
     * 替换文本中的敏感词为指定字符
     * @param text 待处理文本
     * @param replacement 替换字符
     * @return 过滤后的文本
     */
    String replaceSensitiveWords(String text, char replacement);

    /**
     * 过滤消息内容，返回过滤结果
     * @param content 原始内容
     * @return 过滤结果，包含过滤后内容、是否包含敏感词、敏感词列表
     */
    FilterResult filterContent(String content);

    /**
     * 重新加载敏感词库（从数据库动态加载）
     */
    void reloadSensitiveWords();

    /**
     * 添加敏感词
     * @param word 敏感词
     */
    void addSensitiveWord(String word);

    /**
     * 移除敏感词
     * @param word 敏感词
     */
    void removeSensitiveWord(String word);

    /**
     * 过滤结果内部类
     */
    class FilterResult {
        private String filteredContent;
        private boolean hasSensitive;
        private List<String> sensitiveWords;

        public FilterResult(String filteredContent, boolean hasSensitive, List<String> sensitiveWords) {
            this.filteredContent = filteredContent;
            this.hasSensitive = hasSensitive;
            this.sensitiveWords = sensitiveWords;
        }

        public String getFilteredContent() {
            return filteredContent;
        }

        public boolean isHasSensitive() {
            return hasSensitive;
        }

        public List<String> getSensitiveWords() {
            return sensitiveWords;
        }
    }
}
