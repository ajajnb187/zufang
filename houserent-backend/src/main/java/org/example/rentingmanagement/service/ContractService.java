package org.example.rentingmanagement.service;

import org.example.rentingmanagement.entity.RentalContract;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.Map;

/**
 * 电子合同服务接口
 */
public interface ContractService {

    /**
     * 创建合同草稿
     */
    RentalContract createContractDraft(Long houseId, Long landlordId, Long tenantId, String customContent);

    /**
     * 创建合同草稿（完整版本）
     */
    RentalContract createContractDraft(Long houseId, Long landlordId, Long tenantId,
                                       java.time.LocalDate startDate, java.time.LocalDate endDate,
                                       java.math.BigDecimal monthlyRent, java.math.BigDecimal deposit,
                                       String paymentMethod, String customContent);

    /**
     * 生成合同PDF
     */
    String generateContractPDF(Long contractId);

    /**
     * 房东签名
     */
    boolean landlordSign(Long contractId, String signatureData);

    /**
     * 租户签名
     */
    boolean tenantSign(Long contractId, String signatureData);

    /**
     * 管理员审核合同
     */
    boolean auditContract(Long contractId, Long auditorId, boolean approved, String opinion);

    /**
     * 生成合同哈希值（防篡改）
     */
    String generateContractHash(Long contractId);

    /**
     * 验证合同完整性
     */
    boolean verifyContractIntegrity(Long contractId);

    /**
     * 获取合同详情
     */
    RentalContract getContractDetail(Long contractId);

    /**
     * 合同变更
     */
    RentalContract amendContract(Long contractId, String amendmentContent);

    /**
     * 申请合同解约（需管理员审核）
     */
    boolean terminateContract(Long contractId, String terminationReason);

    /**
     * 审核解约申请
     */
    boolean auditTermination(Long contractId, Long auditorId, boolean approved, String opinion);

    /**
     * 获取待审核合同列表
     */
    IPage<RentalContract> getPendingContracts(Long communityId, Integer pageNum, Integer pageSize, String contractNo, String houseTitle);
    
    /**
     * 获取小区合同列表（支持审核状态筛选）
     */
    IPage<RentalContract> getCommunityContracts(Long communityId, String auditStatus, Integer pageNum, Integer pageSize, String contractNo, String landlordName, String tenantName);

    /**
     * 获取用户的合同列表
     */
    IPage<RentalContract> getUserContracts(Long userId, String status, Integer pageNum, Integer pageSize);

    /**
     * 获取房东的合同列表
     */
    IPage<RentalContract> getLandlordContracts(Long landlordId, String status, Integer pageNum, Integer pageSize);

    /**
     * 获取租户的合同列表
     */
    IPage<RentalContract> getTenantContracts(Long tenantId, String status, Integer pageNum, Integer pageSize);

    /**
     * 获取房东的合同统计信息
     */
    Map<String, Object> getLandlordContractStatistics(Long landlordId);

    /**
     * 拒绝合同
     */
    boolean rejectContract(Long contractId, Long userId, String reason);
}
