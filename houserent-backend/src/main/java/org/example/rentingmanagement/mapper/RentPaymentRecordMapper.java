package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.rentingmanagement.entity.RentPaymentRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 租金支付记录数据访问层
 */
@Mapper
public interface RentPaymentRecordMapper extends BaseMapper<RentPaymentRecord> {

    /**
     * 查询房东的收款记录
     */
    @Select("SELECT rpr.*, u.nickname as tenant_name, u.avatar_url as tenant_avatar, h.title as house_title " +
            "FROM rent_payment_records rpr " +
            "LEFT JOIN users u ON rpr.tenant_id = u.user_id " +
            "LEFT JOIN rental_transactions rt ON rpr.transaction_id = rt.transaction_id " +
            "LEFT JOIN houses h ON rt.house_id = h.house_id " +
            "WHERE rpr.landlord_id = #{landlordId} " +
            "ORDER BY rpr.created_at DESC")
    List<RentPaymentRecord> findByLandlordId(@Param("landlordId") Long landlordId);

    /**
     * 查询租客的支付记录
     */
    @Select("SELECT rpr.*, u.nickname as tenant_name, h.title as house_title " +
            "FROM rent_payment_records rpr " +
            "LEFT JOIN users u ON rpr.landlord_id = u.user_id " +
            "LEFT JOIN rental_transactions rt ON rpr.transaction_id = rt.transaction_id " +
            "LEFT JOIN houses h ON rt.house_id = h.house_id " +
            "WHERE rpr.tenant_id = #{tenantId} " +
            "ORDER BY rpr.created_at DESC")
    List<RentPaymentRecord> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 统计房东某个时间段的收益
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM rent_payment_records " +
            "WHERE landlord_id = #{landlordId} AND status = 'confirmed' " +
            "AND payment_period >= #{startPeriod} AND payment_period <= #{endPeriod}")
    BigDecimal sumByLandlordAndPeriod(@Param("landlordId") Long landlordId, 
                                       @Param("startPeriod") String startPeriod,
                                       @Param("endPeriod") String endPeriod);

    /**
     * 统计房东总收益
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM rent_payment_records " +
            "WHERE landlord_id = #{landlordId} AND status = 'confirmed'")
    BigDecimal sumTotalByLandlord(@Param("landlordId") Long landlordId);

    /**
     * 按月统计房东收益
     */
    @Select("SELECT payment_period as period, SUM(amount) as total " +
            "FROM rent_payment_records " +
            "WHERE landlord_id = #{landlordId} AND status = 'confirmed' " +
            "GROUP BY payment_period " +
            "ORDER BY payment_period DESC " +
            "LIMIT 12")
    List<Map<String, Object>> monthlyStatsByLandlord(@Param("landlordId") Long landlordId);

    /**
     * 查询交易的支付记录
     */
    @Select("SELECT * FROM rent_payment_records WHERE transaction_id = #{transactionId} ORDER BY created_at DESC")
    List<RentPaymentRecord> findByTransactionId(@Param("transactionId") Long transactionId);
}
