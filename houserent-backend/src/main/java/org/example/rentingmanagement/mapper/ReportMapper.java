package org.example.rentingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.rentingmanagement.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 举报记录数据访问层
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 查询待处理的举报
     */
    @Select("SELECT * FROM reports WHERE status = 'pending' ORDER BY created_at ASC")
    List<Report> findPendingReports();

    /**
     * 根据举报类型查询
     */
    @Select("SELECT * FROM reports WHERE report_type = #{reportType} AND status = #{status} ORDER BY created_at DESC")
    List<Report> findByTypeAndStatus(String reportType, String status);

    /**
     * 查询用户的举报记录
     */
    @Select("SELECT * FROM reports WHERE reporter_id = #{userId} ORDER BY created_at DESC")
    List<Report> findByReporterId(Long userId);

    /**
     * 查询被举报对象的记录
     */
    @Select("SELECT * FROM reports WHERE report_type = #{reportType} AND target_id = #{targetId}")
    List<Report> findByTarget(String reportType, Long targetId);
}
