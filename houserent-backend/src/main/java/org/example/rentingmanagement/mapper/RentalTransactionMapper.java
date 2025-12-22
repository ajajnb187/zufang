package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.rentingmanagement.entity.RentalTransaction;

import java.util.List;

/**
 * 租赁交易数据访问层
 */
@Mapper
public interface RentalTransactionMapper extends BaseMapper<RentalTransaction> {

    /**
     * 根据用户ID查询交易列表（作为房东或租客）
     */
    @Select("SELECT rt.*, h.title as house_title, c.community_name, " +
            "ul.nickname as landlord_name, ut.nickname as tenant_name, " +
            "ul.phone as landlord_phone, ut.phone as tenant_phone, " +
            "rc.contract_status, rc.contract_no, rc.start_date as contract_start_date, " +
            "rc.end_date as contract_end_date, rc.rent_price " +
            "FROM rental_transactions rt " +
            "LEFT JOIN houses h ON rt.house_id = h.house_id " +
            "LEFT JOIN communities c ON rt.community_id = c.community_id " +
            "LEFT JOIN users ul ON rt.landlord_id = ul.user_id " +
            "LEFT JOIN users ut ON rt.tenant_id = ut.user_id " +
            "LEFT JOIN rental_contracts rc ON rt.contract_id = rc.contract_id " +
            "WHERE rt.landlord_id = #{userId} OR rt.tenant_id = #{userId} " +
            "ORDER BY rt.updated_at DESC")
    List<RentalTransaction> findByUserId(@Param("userId") Long userId);

    /**
     * 根据状态查询用户的交易列表
     */
    @Select("<script>" +
            "SELECT rt.*, h.title as house_title, c.community_name, " +
            "ul.nickname as landlord_name, ut.nickname as tenant_name, " +
            "ul.phone as landlord_phone, ut.phone as tenant_phone, " +
            "rc.contract_status, rc.contract_no, rc.start_date as contract_start_date, " +
            "rc.end_date as contract_end_date, rc.rent_price " +
            "FROM rental_transactions rt " +
            "LEFT JOIN houses h ON rt.house_id = h.house_id " +
            "LEFT JOIN communities c ON rt.community_id = c.community_id " +
            "LEFT JOIN users ul ON rt.landlord_id = ul.user_id " +
            "LEFT JOIN users ut ON rt.tenant_id = ut.user_id " +
            "LEFT JOIN rental_contracts rc ON rt.contract_id = rc.contract_id " +
            "WHERE (rt.landlord_id = #{userId} OR rt.tenant_id = #{userId}) " +
            "<if test='status != null and status != \"\"'>" +
            "AND rt.status = #{status} " +
            "</if>" +
            "ORDER BY rt.updated_at DESC" +
            "</script>")
    List<RentalTransaction> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 查询用户在租中的交易
     */
    @Select("SELECT rt.*, h.title as house_title, c.community_name " +
            "FROM rental_transactions rt " +
            "LEFT JOIN houses h ON rt.house_id = h.house_id " +
            "LEFT JOIN communities c ON rt.community_id = c.community_id " +
            "WHERE (rt.landlord_id = #{userId} OR rt.tenant_id = #{userId}) " +
            "AND rt.status = 'living' " +
            "ORDER BY rt.checkin_date DESC")
    List<RentalTransaction> findLivingByUserId(@Param("userId") Long userId);

    /**
     * 根据合同ID查询交易
     */
    @Select("SELECT * FROM rental_transactions WHERE contract_id = #{contractId}")
    RentalTransaction findByContractId(@Param("contractId") Long contractId);

    /**
     * 获取交易详情（包含完整信息）
     */
    @Select("SELECT rt.*, h.title as house_title, c.community_name, " +
            "ul.nickname as landlord_name, ut.nickname as tenant_name, " +
            "ul.phone as landlord_phone, ut.phone as tenant_phone, " +
            "rc.contract_status, rc.contract_no, rc.start_date as contract_start_date, " +
            "rc.end_date as contract_end_date, rc.rent_price " +
            "FROM rental_transactions rt " +
            "LEFT JOIN houses h ON rt.house_id = h.house_id " +
            "LEFT JOIN communities c ON rt.community_id = c.community_id " +
            "LEFT JOIN users ul ON rt.landlord_id = ul.user_id " +
            "LEFT JOIN users ut ON rt.tenant_id = ut.user_id " +
            "LEFT JOIN rental_contracts rc ON rt.contract_id = rc.contract_id " +
            "WHERE rt.transaction_id = #{transactionId}")
    RentalTransaction findDetailById(@Param("transactionId") Long transactionId);

    /**
     * 查询小区的所有交易（管理员用）
     */
    @Select("SELECT rt.*, h.title as house_title, " +
            "ul.nickname as landlord_name, ut.nickname as tenant_name " +
            "FROM rental_transactions rt " +
            "LEFT JOIN houses h ON rt.house_id = h.house_id " +
            "LEFT JOIN users ul ON rt.landlord_id = ul.user_id " +
            "LEFT JOIN users ut ON rt.tenant_id = ut.user_id " +
            "WHERE rt.community_id = #{communityId} " +
            "ORDER BY rt.updated_at DESC")
    List<RentalTransaction> findByCommunityId(@Param("communityId") Long communityId);
}
