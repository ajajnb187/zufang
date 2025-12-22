package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.rentingmanagement.entity.SensitiveWord;

import java.util.List;

/**
 * 敏感词Mapper接口
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

    @Select("SELECT word FROM sensitive_words WHERE status = 'active'")
    List<String> findAllActiveWords();

    @Select("SELECT word FROM sensitive_words WHERE status = 'active' AND category = #{category}")
    List<String> findWordsByCategory(String category);

    @Select("SELECT word FROM sensitive_word_whitelist WHERE status = 'active'")
    List<String> findAllWhitelistWords();
}
