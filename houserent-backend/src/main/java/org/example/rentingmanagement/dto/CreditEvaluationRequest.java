package org.example.rentingmanagement.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 信用评价请求DTO
 */
@Data
public class CreditEvaluationRequest {

    /**
     * 合同ID
     */
    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    /**
     * 被评价人ID
     */
    @NotNull(message = "被评价人ID不能为空")
    private Long evaluatedId;

    /**
     * 星级评分(0-5)
     */
    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "0", message = "评分不能小于0")
    @DecimalMax(value = "5", message = "评分不能大于5")
    private BigDecimal starRating;

    /**
     * 评价内容
     */
    @Size(max = 500, message = "评价内容不能超过500字符")
    private String content;

    /**
     * 合同履行情况描述
     */
    @Size(max = 1000, message = "履行情况描述不能超过1000字符")
    private String contractPerformance;

    /**
     * 评价标签
     */
    private List<String> tags;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous = false;
}
