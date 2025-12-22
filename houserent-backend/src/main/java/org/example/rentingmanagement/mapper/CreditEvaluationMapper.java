package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.CreditEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 信用评价数据访问层
 */
@Mapper
public interface CreditEvaluationMapper extends BaseMapper<CreditEvaluation> {

    /**
     * 查询用户收到的评价列表
     */
    @Select("SELECT * FROM credit_evaluations WHERE evaluated_id = #{userId} ORDER BY created_at DESC")
    List<CreditEvaluation> findByEvaluatedId(Long userId);

    /**
     * 查询用户的平均评分
     */
    @Select("SELECT AVG(star_rating) FROM credit_evaluations WHERE evaluated_id = #{userId}")
    BigDecimal getAverageRating(Long userId);

    /**
     * 查询用户的评价总数
     */
    @Select("SELECT COUNT(*) FROM credit_evaluations WHERE evaluated_id = #{userId}")
    int getEvaluationCount(Long userId);

    /**
     * 检查是否已评价过某合同
     */
    @Select("SELECT COUNT(*) > 0 FROM credit_evaluations WHERE contract_id = #{contractId} AND evaluator_id = #{evaluatorId}")
    boolean hasEvaluated(Long contractId, Long evaluatorId);

    /**
     * 查询合同的双方评价
     */
    @Select("SELECT * FROM credit_evaluations WHERE contract_id = #{contractId}")
    List<CreditEvaluation> findByContractId(Long contractId);
}
