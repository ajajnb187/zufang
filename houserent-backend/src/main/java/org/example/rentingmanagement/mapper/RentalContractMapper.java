package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.RentalContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 租赁合同数据访问层
 */
@Mapper
public interface RentalContractMapper extends BaseMapper<RentalContract> {

    /**
     * 查询房东的合同列表
     */
    @Select("SELECT * FROM rental_contracts WHERE landlord_id = #{landlordId} ORDER BY created_at DESC")
    List<RentalContract> findByLandlordId(Long landlordId);

    /**
     * 查询租户的合同列表
     */
    @Select("SELECT * FROM rental_contracts WHERE tenant_id = #{tenantId} ORDER BY created_at DESC")
    List<RentalContract> findByTenantId(Long tenantId);

    /**
     * 查询小区待审核的合同
     */
    @Select("SELECT * FROM rental_contracts WHERE community_id = #{communityId} AND audit_status = 'pending' ORDER BY created_at ASC")
    List<RentalContract> findPendingByCommunity(Long communityId);

    /**
     * 根据合同编号查询
     */
    @Select("SELECT * FROM rental_contracts WHERE contract_no = #{contractNo}")
    RentalContract findByContractNo(String contractNo);

    /**
     * 查询即将到期的合同
     */
    @Select("SELECT * FROM rental_contracts WHERE contract_status = 'effective' AND end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)")
    List<RentalContract> findExpiringContracts();
}
