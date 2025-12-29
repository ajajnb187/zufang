package org.example.rentingmanagement.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rentingmanagement.common.BusinessException;
import org.example.rentingmanagement.common.ResultCode;
import org.example.rentingmanagement.entity.House;
import org.example.rentingmanagement.entity.RentalContract;
import org.example.rentingmanagement.entity.User;
import org.example.rentingmanagement.mapper.HouseMapper;
import org.example.rentingmanagement.mapper.RentalContractMapper;
import org.example.rentingmanagement.mapper.UserMapper;
import org.example.rentingmanagement.service.ContractService;
import org.example.rentingmanagement.service.FileUploadService;
import org.example.rentingmanagement.service.SystemNotificationService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 电子合同服务实现类
 * 基于iText 5.5.13 + 数字签名最新技术方案
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl extends ServiceImpl<RentalContractMapper, RentalContract> implements ContractService {

    private final HouseMapper houseMapper;
    private final UserMapper userMapper;
    private final FileUploadService fileUploadService;
    private final org.example.rentingmanagement.mapper.RentalTransactionMapper rentalTransactionMapper;
    private final SystemNotificationService systemNotificationService;

    /**
     * 合同模板内容
     */
    private static final String CONTRACT_TEMPLATE = """
            房屋租赁合同
            
            甲方（出租方）：%s
            乙方（承租方）：%s
            
            根据《中华人民共和国合同法》及相关法律法规，甲乙双方在平等、自愿、公平和诚实信用的基础上，
            就乙方承租甲方房屋事宜，为明确双方权利义务关系，经协商一致，订立本合同。
            
            第一条 房屋基本情况
            甲方房屋位于：%s
            房屋类型：%s
            建筑面积：%.2f平方米
            房屋朝向：%s
            装修情况：%s
            
            第二条 租赁期限
            租赁期限自%s至%s，共计%d个月。
            
            第三条 租金
            月租金为人民币%s元（大写：%s）。
            租金支付方式：%s
            
            第四条 押金
            乙方应于签订本合同时向甲方支付房屋押金，押金数额为%s元。
            
            第五条 甲方权利和义务
            1. 甲方应当按时将房屋及附属设施交付乙方使用。
            2. 甲方保证房屋的建筑结构和设备设施符合建筑、消防、治安、卫生等方面的安全条件。
            3. 租赁期间，甲方不得随意进入出租房屋。
            
            第六条 乙方权利和义务
            1. 乙方应当按照约定的用途使用房屋，不得擅自改变使用用途。
            2. 乙方应当爱护并合理使用房屋及其附属设施。
            3. 乙方应当按时支付租金。
            
            第七条 补充条款
            %s
            
            第八条 违约责任
            甲乙双方应当严格履行本合同约定的义务，如一方违约，应承担相应的违约责任。
            
            第九条 争议解决
            本合同履行过程中发生的争议，由双方协商解决；协商不成的，可向房屋所在地人民法院起诉。
            
            第十条 合同生效
            本合同自甲乙双方签字盖章后生效。
            
            甲方（出租方）签名：_________________ 日期：_________
            
            乙方（承租方）签名：_________________ 日期：_________
            
            小区管理员审核：_________________ 日期：_________
            """;

    @Override
    public RentalContract createContractDraft(Long houseId, Long landlordId, Long tenantId, String customContent) {
        try {
            // 验证房源信息
            House house = houseMapper.selectById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 验证用户信息
            User landlord = userMapper.selectById(landlordId);
            User tenant = userMapper.selectById(tenantId);
            if (landlord == null || tenant == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
            }

            // 生成合同编号
            String contractNo = generateContractNo();

            // 创建合同实体
            RentalContract contract = new RentalContract();
            contract.setContractNo(contractNo);
            contract.setHouseId(houseId);
            contract.setLandlordId(landlordId);
            contract.setTenantId(tenantId);
            contract.setCommunityId(house.getCommunityId());
            contract.setTemplateContent(CONTRACT_TEMPLATE);
            contract.setCustomContent(customContent);
            contract.setRentPrice(house.getRentPrice());
            contract.setPaymentMethod(house.getPaymentMethod());
            contract.setStartDate(LocalDate.now().plusDays(1)); // 明天开始
            contract.setEndDate(LocalDate.now().plusYears(1)); // 一年期限
            contract.setAuditStatus("draft");
            contract.setContractStatus("draft");

            // 保存合同
            this.save(contract);

            log.info("合同草稿创建成功: contractId={}, contractNo={}", contract.getContractId(), contractNo);
            return contract;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建合同草稿失败: {}", e.getMessage(), e);
            throw new BusinessException("创建合同草稿失败");
        }
    }

    @Override
    public RentalContract createContractDraft(Long houseId, Long landlordId, Long tenantId,
                                              LocalDate startDate, LocalDate endDate,
                                              java.math.BigDecimal monthlyRent, java.math.BigDecimal deposit,
                                              String paymentMethod, String customContent) {
        try {
            // 验证房源信息
            House house = houseMapper.selectById(houseId);
            if (house == null) {
                throw new BusinessException(ResultCode.HOUSE_NOT_FOUND, "房源不存在");
            }

            // 验证用户信息
            User landlord = userMapper.selectById(landlordId);
            User tenant = userMapper.selectById(tenantId);
            if (landlord == null || tenant == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
            }

            // 生成合同编号
            String contractNo = generateContractNo();

            // 创建合同实体
            RentalContract contract = new RentalContract();
            contract.setContractNo(contractNo);
            contract.setHouseId(houseId);
            contract.setLandlordId(landlordId);
            contract.setTenantId(tenantId);
            contract.setCommunityId(house.getCommunityId());
            contract.setTemplateContent(CONTRACT_TEMPLATE);
            contract.setCustomContent(customContent);
            
            // 使用前端传入的租金和押金，如果没有则使用房源默认值
            contract.setRentPrice(monthlyRent != null ? monthlyRent : house.getRentPrice());
            contract.setDeposit(deposit);
            contract.setPaymentMethod(paymentMethod != null ? paymentMethod : house.getPaymentMethod());
            
            // 使用前端传入的日期
            contract.setStartDate(startDate != null ? startDate : LocalDate.now().plusDays(1));
            contract.setEndDate(endDate != null ? endDate : LocalDate.now().plusYears(1));
            
            contract.setAuditStatus("draft"); // 草稿状态，待双方签署
            contract.setContractStatus("draft");

            // 保存合同
            this.save(contract);
            
            // 发送WebSocket通知给租客
            try {
                String notifyContent = "房东 " + landlord.getNickname() + " 向您发起了一份租赁合同，请及时查看并确认签署";
                systemNotificationService.createNotification(
                    tenantId, 
                    "contract_created", 
                    "新合同待签署", 
                    notifyContent, 
                    "contract", 
                    contract.getContractId()
                );
                log.info("合同创建通知已发送给租客: tenantId={}, contractId={}", tenantId, contract.getContractId());
            } catch (Exception e) {
                log.warn("发送合同创建通知失败: {}", e.getMessage());
            }

            log.info("合同草稿创建成功(完整版): contractId={}, contractNo={}, tenant={}", 
                    contract.getContractId(), contractNo, tenantId);
            return contract;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建合同草稿失败: {}", e.getMessage(), e);
            throw new BusinessException("创建合同草稿失败");
        }
    }

    @Override
    public String generateContractPDF(Long contractId) {
        try {
            RentalContract contract = this.getById(contractId);
            if (contract == null) {
                throw new BusinessException(ResultCode.CONTRACT_NOT_FOUND, "合同不存在");
            }

            // 获取相关信息
            House house = houseMapper.selectById(contract.getHouseId());
            User landlord = userMapper.selectById(contract.getLandlordId());
            User tenant = userMapper.selectById(contract.getTenantId());

            // 创建PDF文档
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();

            // 设置中文字体
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font contentFont = new Font(bfChinese, 12, Font.NORMAL);

            // 添加标题
            Paragraph title = new Paragraph("房屋租赁合同", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 格式化合同内容
            String formattedContent = String.format(
                    CONTRACT_TEMPLATE,
                    landlord.getNickname() != null ? landlord.getNickname() : "房东",
                    tenant.getNickname() != null ? tenant.getNickname() : "租户",
                    house.getTitle(),
                    house.getHouseType(),
                    house.getArea(),
                    house.getOrientation() != null ? house.getOrientation() : "朝南",
                    house.getDecoration() != null ? house.getDecoration() : "精装修",
                    contract.getStartDate().toString(),
                    contract.getEndDate().toString(),
                    calculateMonthsBetween(contract.getStartDate(), contract.getEndDate()),
                    contract.getRentPrice(),
                    convertNumberToChinese(contract.getRentPrice()),
                    contract.getPaymentMethod(),
                    contract.getRentPrice(),
                    contract.getCustomContent() != null ? contract.getCustomContent() : ""
            );

            // 添加合同内容
            Paragraph content = new Paragraph(formattedContent, contentFont);
            content.setSpacingAfter(10);
            document.add(content);

            // 添加签名区域
            document.add(new Paragraph(" ", contentFont)); // 空行
            
            // 甲方签名
            if (contract.getLandlordSignature() != null && !contract.getLandlordSignature().isEmpty()) {
                addSignatureToDocument(document, "甲方（出租方）签名", contract.getLandlordSignature(), 
                    contract.getLandlordSignTime(), bfChinese);
            } else {
                Paragraph noSign = new Paragraph("甲方（出租方）签名：_________________ 日期：_________", contentFont);
                document.add(noSign);
            }
            
            document.add(new Paragraph(" ", contentFont)); // 空行

            // 乙方签名
            if (contract.getTenantSignature() != null && !contract.getTenantSignature().isEmpty()) {
                addSignatureToDocument(document, "乙方（承租方）签名", contract.getTenantSignature(), 
                    contract.getTenantSignTime(), bfChinese);
            } else {
                Paragraph noSign = new Paragraph("乙方（承租方）签名：_________________ 日期：_________", contentFont);
                document.add(noSign);
            }
            
            document.add(new Paragraph(" ", contentFont)); // 空行
            
            // 小区管理员审核
            if (contract.getAuditorId() != null && contract.getAuditTime() != null) {
                // 获取审核员信息
                User auditor = userMapper.selectById(contract.getAuditorId());
                String auditorName = auditor != null ? (auditor.getNickname() != null ? auditor.getNickname() : "管理员") : "管理员";
                String auditDateStr = contract.getAuditTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String auditStatus = "approved".equals(contract.getAuditStatus()) ? "审核通过" : "审核中";
                
                Paragraph auditLabel = new Paragraph("小区管理员审核：", contentFont);
                document.add(auditLabel);
                Paragraph auditInfo = new Paragraph("审核人：" + auditorName + "    状态：" + auditStatus, contentFont);
                document.add(auditInfo);
                Paragraph auditDate = new Paragraph("审核日期：" + auditDateStr, contentFont);
                document.add(auditDate);
                if (contract.getAuditOpinion() != null && !contract.getAuditOpinion().isEmpty()) {
                    Paragraph auditOpinion = new Paragraph("审核意见：" + contract.getAuditOpinion(), contentFont);
                    document.add(auditOpinion);
                }
            } else {
                Paragraph noAudit = new Paragraph("小区管理员审核：_________________ 日期：_________", contentFont);
                document.add(noAudit);
            }

            document.close();

            // 保存PDF文件
            String fileName = "contract_" + contract.getContractNo() + ".pdf";
            String filePath = saveContractPDF(fileName, baos.toByteArray());

            // 更新合同PDF路径
            contract.setPdfUrl(filePath);
            this.updateById(contract);

            log.info("合同PDF生成成功: contractId={}, filePath={}", contractId, filePath);
            return filePath;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成合同PDF失败: {}", e.getMessage(), e);
            throw new BusinessException("生成合同PDF失败");
        }
    }

    @Override
    public boolean landlordSign(Long contractId, String signatureData) {
        try {
            RentalContract contract = this.getById(contractId);
            if (contract == null) {
                throw new BusinessException(ResultCode.CONTRACT_NOT_FOUND, "合同不存在");
            }

            if (!"draft".equals(contract.getContractStatus())) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "合同状态错误，无法签名");
            }

            // 保存房东签名
            contract.setLandlordSignature(signatureData);
            contract.setLandlordSignTime(LocalDateTime.now());

            // 检查双方是否都已签名
            if (contract.getTenantSignature() != null) {
                contract.setAuditStatus("pending");
                contract.setContractStatus("signed");
                // 生成合同哈希值
                contract.setContractHash(generateContractHash(contractId));
            }

            this.updateById(contract);

            log.info("房东签名成功: contractId={}", contractId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("房东签名失败: {}", e.getMessage(), e);
            throw new BusinessException("房东签名失败");
        }
    }

    @Override
    public boolean tenantSign(Long contractId, String signatureData) {
        try {
            RentalContract contract = this.getById(contractId);
            if (contract == null) {
                throw new BusinessException(ResultCode.CONTRACT_NOT_FOUND, "合同不存在");
            }

            if (!"draft".equals(contract.getContractStatus())) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "合同状态错误，无法签名");
            }

            // 保存租户签名
            contract.setTenantSignature(signatureData);
            contract.setTenantSignTime(LocalDateTime.now());

            // 检查双方是否都已签名
            if (contract.getLandlordSignature() != null) {
                contract.setAuditStatus("pending");
                contract.setContractStatus("signed");
                // 生成合同哈希值
                contract.setContractHash(generateContractHash(contractId));
            }

            this.updateById(contract);

            log.info("租户签名成功: contractId={}", contractId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("租户签名失败: {}", e.getMessage(), e);
            throw new BusinessException("租户签名失败");
        }
    }

    @Override
    public boolean auditContract(Long contractId, Long auditorId, boolean approved, String opinion) {
        try {
            RentalContract contract = this.getById(contractId);
            if (contract == null) {
                throw new BusinessException(ResultCode.CONTRACT_NOT_FOUND, "合同不存在");
            }

            // 检查合同状态：必须是已签署状态才能审核
            if (!"signed".equals(contract.getContractStatus())) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "合同尚未完成签署，无法审核");
            }
            
            if (!"pending".equals(contract.getAuditStatus())) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "合同审核状态错误，无法审核");
            }
            
            // 检查双方是否都已签名
            if (contract.getLandlordSignature() == null || contract.getLandlordSignature().isEmpty()) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "房东尚未签名，无法审核通过");
            }
            if (contract.getTenantSignature() == null || contract.getTenantSignature().isEmpty()) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "租客尚未签名，无法审核通过");
            }

            // 更新审核信息
            contract.setAuditorId(auditorId);
            contract.setAuditOpinion(opinion);
            contract.setAuditTime(LocalDateTime.now());

            if (approved) {
                contract.setAuditStatus("approved");
                contract.setContractStatus("effective");
                // 重新生成PDF（包含审核信息）
                generateContractPDF(contractId);
                
                // 创建租赁交易记录
                createTransactionFromContract(contract);
            } else {
                contract.setAuditStatus("rejected");
                contract.setContractStatus("draft"); // 驳回后回到草稿状态，需要重新签署
            }

            this.updateById(contract);

            log.info("合同审核完成: contractId={}, approved={}", contractId, approved);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("合同审核失败: {}", e.getMessage(), e);
            throw new BusinessException("合同审核失败");
        }
    }

    @Override
    public String generateContractHash(Long contractId) {
        try {
            RentalContract contract = this.getById(contractId);
            if (contract == null) {
                throw new BusinessException(ResultCode.CONTRACT_NOT_FOUND, "合同不存在");
            }

            // 构造用于哈希的字符串
            StringBuilder hashInput = new StringBuilder();
            hashInput.append(contract.getContractNo());
            hashInput.append(contract.getTemplateContent());
            hashInput.append(contract.getCustomContent() != null ? contract.getCustomContent() : "");
            hashInput.append(contract.getLandlordSignature() != null ? contract.getLandlordSignature() : "");
            hashInput.append(contract.getTenantSignature() != null ? contract.getTenantSignature() : "");
            hashInput.append(contract.getRentPrice());
            hashInput.append(contract.getStartDate());
            hashInput.append(contract.getEndDate());

            // 生成SHA-256哈希值
            String hash = DigestUtil.sha256Hex(hashInput.toString());
            
            log.info("合同哈希值生成: contractId={}, hash={}", contractId, hash);
            return hash;

        } catch (Exception e) {
            log.error("生成合同哈希值失败: {}", e.getMessage(), e);
            throw new BusinessException("生成合同哈希值失败");
        }
    }

    @Override
    public boolean verifyContractIntegrity(Long contractId) {
        try {
            RentalContract contract = this.getById(contractId);
            if (contract == null || contract.getContractHash() == null) {
                return false;
            }

            // 重新生成哈希值并比较
            String currentHash = generateContractHash(contractId);
            boolean isValid = currentHash.equals(contract.getContractHash());

            log.info("合同完整性验证: contractId={}, valid={}", contractId, isValid);
            return isValid;

        } catch (Exception e) {
            log.error("验证合同完整性失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public RentalContract getContractDetail(Long contractId) {
        RentalContract contract = this.getById(contractId);
        if (contract == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "合同不存在");
        }
        
        // 填充房源信息
        House house = houseMapper.selectById(contract.getHouseId());
        if (house != null) {
            contract.setHouseTitle(house.getTitle());
            contract.setHouseType(house.getHouseType());
            contract.setHouseArea(house.getArea());
            // 获取第一张图片作为封面
            if (house.getImages() != null && !house.getImages().isEmpty()) {
                String images = house.getImages();
                if (images.startsWith("[")) {
                    // JSON数组格式
                    images = images.replace("[", "").replace("]", "").replace("\"", "");
                    String[] imgArr = images.split(",");
                    if (imgArr.length > 0) {
                        contract.setHouseImage(imgArr[0].trim());
                    }
                } else {
                    contract.setHouseImage(images);
                }
            }
        }
        
        // 填充房东信息
        User landlord = userMapper.selectById(contract.getLandlordId());
        if (landlord != null) {
            contract.setLandlordName(landlord.getNickname());
            contract.setLandlordPhone(landlord.getPhone());
            contract.setLandlordAvatar(landlord.getAvatarUrl());
        }
        
        // 填充租客信息
        User tenant = userMapper.selectById(contract.getTenantId());
        if (tenant != null) {
            contract.setTenantName(tenant.getNickname());
            contract.setTenantPhone(tenant.getPhone());
            contract.setTenantAvatar(tenant.getAvatarUrl());
        }
        
        return contract;
    }

    @Override
    public RentalContract amendContract(Long contractId, String amendmentContent) {
        try {
            RentalContract contract = this.getById(contractId);
            if (contract == null) {
                throw new BusinessException(ResultCode.CONTRACT_NOT_FOUND, "合同不存在");
            }

            if (!"effective".equals(contract.getContractStatus())) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "只有生效的合同才能变更");
            }

            // 创建变更合同
            RentalContract amendedContract = new RentalContract();
            amendedContract.setContractNo(generateContractNo());
            amendedContract.setHouseId(contract.getHouseId());
            amendedContract.setLandlordId(contract.getLandlordId());
            amendedContract.setTenantId(contract.getTenantId());
            amendedContract.setCommunityId(contract.getCommunityId());
            amendedContract.setTemplateContent(contract.getTemplateContent());
            amendedContract.setCustomContent(contract.getCustomContent() + "\n\n变更条款：\n" + amendmentContent);
            amendedContract.setRentPrice(contract.getRentPrice());
            amendedContract.setPaymentMethod(contract.getPaymentMethod());
            amendedContract.setStartDate(contract.getStartDate());
            amendedContract.setEndDate(contract.getEndDate());
            amendedContract.setAuditStatus("draft");
            amendedContract.setContractStatus("draft");

            this.save(amendedContract);

            log.info("合同变更成功: 原合同={}, 新合同={}", contractId, amendedContract.getContractId());
            return amendedContract;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("合同变更失败: {}", e.getMessage(), e);
            throw new BusinessException("合同变更失败");
        }
    }

    @Override
    public boolean terminateContract(Long contractId, String terminationReason) {
        try {
            RentalContract contract = this.getById(contractId);
            if (contract == null) {
                throw new BusinessException(ResultCode.CONTRACT_NOT_FOUND, "合同不存在");
            }

            if (!"effective".equals(contract.getContractStatus())) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "只有生效的合同才能申请解约");
            }
            
            if ("pending".equals(contract.getTerminationStatus())) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "已有解约申请待审核");
            }

            // 提交解约申请，等待管理员审核
            Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
            contract.setTerminationReason(terminationReason);
            contract.setTerminationRequestedBy(userId);
            contract.setTerminationRequestTime(LocalDateTime.now());
            contract.setTerminationStatus("pending");
            this.updateById(contract);

            log.info("解约申请已提交: contractId={}, reason={}, requestedBy={}", contractId, terminationReason, userId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解约申请提交失败: {}", e.getMessage(), e);
            throw new BusinessException("解约申请提交失败");
        }
    }
    
    /**
     * 审核解约申请
     */
    public boolean auditTermination(Long contractId, Long auditorId, boolean approved, String opinion) {
        try {
            RentalContract contract = this.getById(contractId);
            if (contract == null) {
                throw new BusinessException(ResultCode.CONTRACT_NOT_FOUND, "合同不存在");
            }
            
            if (!"pending".equals(contract.getTerminationStatus())) {
                throw new BusinessException(ResultCode.CONTRACT_STATUS_ERROR, "没有待审核的解约申请");
            }
            
            if (approved) {
                contract.setTerminationStatus("approved");
                contract.setContractStatus("terminated");
                contract.setAuditOpinion("解约审核通过：" + (opinion != null ? opinion : ""));
                
                // 同步更新交易状态为已完成
                syncTransactionStatusOnTerminate(contractId);
            } else {
                contract.setTerminationStatus("rejected");
                contract.setAuditOpinion("解约申请被驳回：" + (opinion != null ? opinion : ""));
            }
            contract.setAuditorId(auditorId);
            contract.setAuditTime(LocalDateTime.now());
            this.updateById(contract);
            
            log.info("解约审核完成: contractId={}, approved={}", contractId, approved);
            return true;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解约审核失败: {}", e.getMessage(), e);
            throw new BusinessException("解约审核失败");
        }
    }

    /**
     * 合同终止时同步更新交易状态
     */
    private void syncTransactionStatusOnTerminate(Long contractId) {
        try {
            org.example.rentingmanagement.entity.RentalTransaction transaction = 
                rentalTransactionMapper.findByContractId(contractId);
            if (transaction != null) {
                // 只有非完成状态的交易才需要更新
                String status = transaction.getStatus();
                if (!"completed".equals(status) && !"evaluated".equals(status) && !"cancelled".equals(status)) {
                    transaction.setStatus("completed");
                    transaction.setCheckoutRemark("合同已终止");
                    rentalTransactionMapper.updateById(transaction);
                    log.info("合同终止，同步更新交易状态为已完成: contractId={}, transactionId={}", 
                        contractId, transaction.getTransactionId());
                }
            }
        } catch (Exception e) {
            log.error("同步交易状态失败: contractId={}", contractId, e);
        }
    }

    /**
     * 生成合同编号
     */
    private String generateContractNo() {
        String dateStr = DateUtil.format(DateUtil.date(), "yyyyMMdd");
        String randomStr = IdUtil.randomUUID().substring(0, 8).toUpperCase();
        return "CONTRACT_" + dateStr + "_" + randomStr;
    }

    /**
     * 计算两个日期之间的月数
     */
    private int calculateMonthsBetween(LocalDate start, LocalDate end) {
        return (int) java.time.temporal.ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * 数字转中文
     */
    private String convertNumberToChinese(java.math.BigDecimal number) {
        // 简化实现，实际项目中需要完整的数字转中文逻辑
        return number + "元整";
    }

    /**
     * 在文档中添加签名图片和日期
     */
    private void addSignatureToDocument(Document document, String label, String signatureBase64, 
            LocalDateTime signTime, BaseFont font) throws DocumentException, IOException {
        Font labelFont = new Font(font, 12, Font.NORMAL);
        
        // 添加签名标签
        Paragraph signatureLabel = new Paragraph(label + "：", labelFont);
        document.add(signatureLabel);

        // 解析Base64签名图片并添加到PDF
        if (signatureBase64 != null && !signatureBase64.isEmpty()) {
            try {
                // 移除Base64前缀 (data:image/png;base64,)
                String base64Data = signatureBase64;
                if (base64Data.contains(",")) {
                    base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                }
                
                // 解码Base64为字节数组
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                
                // 创建iText图片
                Image signatureImage = Image.getInstance(imageBytes);
                
                // 设置图片大小（宽度150，高度自动缩放）
                signatureImage.scaleToFit(150, 60);
                
                // 添加图片到文档
                document.add(signatureImage);
                
            } catch (Exception e) {
                log.warn("签名图片解析失败，使用文字替代: {}", e.getMessage());
                Paragraph signatureText = new Paragraph("【电子签名已确认】", labelFont);
                document.add(signatureText);
            }
        }
        
        // 添加签署日期
        String dateStr = "_________";
        if (signTime != null) {
            dateStr = signTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        Paragraph dateParagraph = new Paragraph("签署日期：" + dateStr, labelFont);
        document.add(dateParagraph);
        
        document.add(new Paragraph(" ", labelFont)); // 空行
    }

    /**
     * 保存合同PDF文件
     */
    private String saveContractPDF(String fileName, byte[] pdfData) throws IOException {
        // 创建contracts目录
        Path contractsDir = Paths.get("contracts");
        if (!Files.exists(contractsDir)) {
            Files.createDirectories(contractsDir);
        }

        // 保存文件
        Path filePath = contractsDir.resolve(fileName);
        Files.write(filePath, pdfData);

        return filePath.toString();
    }

    @Override
    public IPage<RentalContract> getPendingContracts(Long communityId, Integer pageNum, Integer pageSize, String contractNo, String houseTitle) {
        try {
            Page<RentalContract> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<RentalContract> wrapper = new LambdaQueryWrapper<>();
            
            wrapper.eq(RentalContract::getAuditStatus, "pending")
                   .eq(communityId != null, RentalContract::getCommunityId, communityId)
                   .like(contractNo != null && !contractNo.trim().isEmpty(), RentalContract::getContractNo, contractNo)
                   .orderByDesc(RentalContract::getCreatedAt);
            
            IPage<RentalContract> result = this.page(page, wrapper);
            // 填充关联数据
            for (RentalContract contract : result.getRecords()) {
                fillContractRelatedData(contract);
            }
            return result;
        } catch (Exception e) {
            log.error("获取待审核合同列表失败: {}", e.getMessage(), e);
            throw new BusinessException("获取待审核合同失败");
        }
    }
    
    @Override
    public IPage<RentalContract> getCommunityContracts(Long communityId, String auditStatus, Integer pageNum, Integer pageSize, 
                                                        String contractNo, String landlordName, String tenantName) {
        try {
            Page<RentalContract> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<RentalContract> wrapper = new LambdaQueryWrapper<>();
            
            // 必须指定小区
            wrapper.eq(communityId != null, RentalContract::getCommunityId, communityId)
                   // 审核状态筛选
                   .eq(auditStatus != null && !auditStatus.trim().isEmpty(), RentalContract::getAuditStatus, auditStatus)
                   // 合同编号模糊搜索
                   .like(contractNo != null && !contractNo.trim().isEmpty(), RentalContract::getContractNo, contractNo)
                   .orderByDesc(RentalContract::getCreatedAt);
            
            IPage<RentalContract> result = this.page(page, wrapper);
            
            // 填充关联数据并进行房东/租客名称筛选
            java.util.List<RentalContract> filteredRecords = new java.util.ArrayList<>();
            for (RentalContract contract : result.getRecords()) {
                fillContractRelatedData(contract);
                
                // 房东名称筛选
                if (landlordName != null && !landlordName.trim().isEmpty()) {
                    if (contract.getLandlordName() == null || !contract.getLandlordName().contains(landlordName)) {
                        continue;
                    }
                }
                // 租客名称筛选
                if (tenantName != null && !tenantName.trim().isEmpty()) {
                    if (contract.getTenantName() == null || !contract.getTenantName().contains(tenantName)) {
                        continue;
                    }
                }
                filteredRecords.add(contract);
            }
            
            result.setRecords(filteredRecords);
            return result;
        } catch (Exception e) {
            log.error("获取小区合同列表失败: {}", e.getMessage(), e);
            throw new BusinessException("获取小区合同列表失败");
        }
    }

    @Override
    public IPage<RentalContract> getUserContracts(Long userId, String status, Integer pageNum, Integer pageSize) {
        try {
            Page<RentalContract> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<RentalContract> wrapper = new LambdaQueryWrapper<>();
            wrapper.and(w -> w.eq(RentalContract::getLandlordId, userId).or().eq(RentalContract::getTenantId, userId))
                   .eq(status != null && !status.trim().isEmpty(), RentalContract::getContractStatus, status)
                   .orderByDesc(RentalContract::getCreatedAt);
            IPage<RentalContract> result = this.page(page, wrapper);
            // 填充关联数据
            for (RentalContract contract : result.getRecords()) {
                fillContractRelatedData(contract);
            }
            return result;
        } catch (Exception e) {
            log.error("获取用户合同列表失败: {}", e.getMessage(), e);
            throw new BusinessException("获取用户合同列表失败");
        }
    }

    @Override
    public IPage<RentalContract> getLandlordContracts(Long landlordId, String status, Integer pageNum, Integer pageSize) {
        try {
            log.info("获取房东合同列表: landlordId={}, status={}, pageNum={}, pageSize={}", landlordId, status, pageNum, pageSize);
            
            Page<RentalContract> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<RentalContract> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RentalContract::getLandlordId, landlordId);
            
            // 只有当status有有效值时才过滤
            if (status != null && !status.trim().isEmpty() && !"null".equals(status)) {
                wrapper.eq(RentalContract::getContractStatus, status);
                log.info("按contract_status过滤: {}", status);
            }
            
            wrapper.orderByDesc(RentalContract::getCreatedAt);
            IPage<RentalContract> result = this.page(page, wrapper);
            
            log.info("查询到合同数量: {}", result.getRecords().size());
            
            // 填充关联数据
            for (RentalContract contract : result.getRecords()) {
                log.info("合同: id={}, contractNo={}, contractStatus={}, auditStatus={}", 
                    contract.getContractId(), contract.getContractNo(), 
                    contract.getContractStatus(), contract.getAuditStatus());
                fillContractRelatedData(contract);
            }
            return result;
        } catch (Exception e) {
            log.error("获取房东合同列表失败: {}", e.getMessage(), e);
            throw new BusinessException("获取房东合同列表失败");
        }
    }

    @Override
    public IPage<RentalContract> getTenantContracts(Long tenantId, String status, Integer pageNum, Integer pageSize) {
        try {
            Page<RentalContract> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<RentalContract> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RentalContract::getTenantId, tenantId)
                   .eq(status != null && !status.trim().isEmpty(), RentalContract::getContractStatus, status)
                   .orderByDesc(RentalContract::getCreatedAt);
            IPage<RentalContract> result = this.page(page, wrapper);
            // 填充关联数据
            for (RentalContract contract : result.getRecords()) {
                fillContractRelatedData(contract);
            }
            return result;
        } catch (Exception e) {
            log.error("获取租户合同列表失败: {}", e.getMessage(), e);
            throw new BusinessException("获取租户合同列表失败");
        }
    }
    
    /**
     * 填充合同关联数据（房源、房东、租客信息）
     */
    private void fillContractRelatedData(RentalContract contract) {
        // 填充房源信息
        House house = houseMapper.selectById(contract.getHouseId());
        if (house != null) {
            contract.setHouseTitle(house.getTitle());
            contract.setHouseType(house.getHouseType());
            contract.setHouseArea(house.getArea());
            // 获取第一张图片作为封面
            if (house.getImages() != null && !house.getImages().isEmpty()) {
                String images = house.getImages();
                if (images.startsWith("[")) {
                    images = images.replace("[", "").replace("]", "").replace("\"", "");
                    String[] imgArr = images.split(",");
                    if (imgArr.length > 0) {
                        contract.setHouseImage(imgArr[0].trim());
                    }
                } else {
                    contract.setHouseImage(images);
                }
            }
        }
        
        // 填充房东信息
        User landlord = userMapper.selectById(contract.getLandlordId());
        if (landlord != null) {
            contract.setLandlordName(landlord.getNickname());
            contract.setLandlordPhone(landlord.getPhone());
            contract.setLandlordAvatar(landlord.getAvatarUrl());
        }
        
        // 填充租客信息
        User tenant = userMapper.selectById(contract.getTenantId());
        if (tenant != null) {
            contract.setTenantName(tenant.getNickname());
            contract.setTenantPhone(tenant.getPhone());
            contract.setTenantAvatar(tenant.getAvatarUrl());
        }
    }

    @Override
    public Map<String, Object> getLandlordContractStatistics(Long landlordId) {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总合同数
            LambdaQueryWrapper<RentalContract> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(RentalContract::getLandlordId, landlordId);
            long totalContracts = this.count(totalWrapper);
            statistics.put("totalContracts", (int) totalContracts);
            
            // 有效合同数
            LambdaQueryWrapper<RentalContract> activeWrapper = new LambdaQueryWrapper<>();
            activeWrapper.eq(RentalContract::getLandlordId, landlordId)
                        .eq(RentalContract::getContractStatus, "effective");
            long activeContracts = this.count(activeWrapper);
            statistics.put("activeContracts", (int) activeContracts);
            
            // 已过期合同数
            LambdaQueryWrapper<RentalContract> expiredWrapper = new LambdaQueryWrapper<>();
            expiredWrapper.eq(RentalContract::getLandlordId, landlordId)
                         .eq(RentalContract::getContractStatus, "expired");
            long expiredContracts = this.count(expiredWrapper);
            statistics.put("expiredContracts", (int) expiredContracts);
            
            // 待签署合同数（草稿状态）
            LambdaQueryWrapper<RentalContract> draftWrapper = new LambdaQueryWrapper<>();
            draftWrapper.eq(RentalContract::getLandlordId, landlordId)
                       .eq(RentalContract::getContractStatus, "draft");
            long draftContracts = this.count(draftWrapper);
            statistics.put("draftContracts", (int) draftContracts);
            
            // 待审核合同数（已签署待审核）
            LambdaQueryWrapper<RentalContract> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(RentalContract::getLandlordId, landlordId)
                         .eq(RentalContract::getContractStatus, "signed")
                         .eq(RentalContract::getAuditStatus, "pending");
            long pendingContracts = this.count(pendingWrapper);
            statistics.put("pendingContracts", (int) pendingContracts);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取房东合同统计失败: landlordId={}", landlordId, e);
            // 返回默认值
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalContracts", 0);
            defaultStats.put("activeContracts", 0);
            defaultStats.put("expiredContracts", 0);
            defaultStats.put("pendingContracts", 0);
            return defaultStats;
        }
    }

    @Override
    public boolean rejectContract(Long contractId, Long userId, String reason) {
        try {
            RentalContract contract = this.getById(contractId);
            if (contract == null) {
                throw new BusinessException("合同不存在");
            }
            
            // 验证用户是否有权限拒绝（只有租户可以拒绝）
            if (!contract.getTenantId().equals(userId)) {
                throw new BusinessException("您没有权限拒绝此合同");
            }
            
            // 只有待签署状态的合同可以拒绝
            if (!"draft".equals(contract.getContractStatus()) && !"pending".equals(contract.getAuditStatus())) {
                throw new BusinessException("当前合同状态不允许拒绝");
            }
            
            // 更新合同状态
            contract.setContractStatus("terminated");
            contract.setAuditStatus("rejected");
            if (reason != null && !reason.isEmpty()) {
                contract.setAuditOpinion("租户拒绝签署：" + reason);
            } else {
                contract.setAuditOpinion("租户拒绝签署");
            }
            
            this.updateById(contract);
            log.info("租户拒绝合同: contractId={}, userId={}, reason={}", contractId, userId, reason);
            return true;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("拒绝合同失败: contractId={}, userId={}", contractId, userId, e);
            throw new BusinessException("拒绝合同失败");
        }
    }

    /**
     * 根据合同创建交易记录
     */
    private void createTransactionFromContract(RentalContract contract) {
        try {
            // 检查是否已有交易记录
            org.example.rentingmanagement.entity.RentalTransaction existing = 
                rentalTransactionMapper.findByContractId(contract.getContractId());
            if (existing != null) {
                log.info("交易记录已存在: contractId={}, transactionId={}", 
                        contract.getContractId(), existing.getTransactionId());
                return;
            }

            // 创建交易记录
            org.example.rentingmanagement.entity.RentalTransaction transaction = 
                new org.example.rentingmanagement.entity.RentalTransaction();
            transaction.setContractId(contract.getContractId());
            transaction.setHouseId(contract.getHouseId());
            transaction.setLandlordId(contract.getLandlordId());
            transaction.setTenantId(contract.getTenantId());
            transaction.setCommunityId(contract.getCommunityId());
            transaction.setStatus("signed"); // 合同审核通过，进入已签署待入住状态
            transaction.setLandlordCheckinConfirm(false);
            transaction.setTenantCheckinConfirm(false);
            transaction.setLandlordCompleteConfirm(false);
            transaction.setTenantCompleteConfirm(false);
            transaction.setLandlordEvaluated(false);
            transaction.setTenantEvaluated(false);

            rentalTransactionMapper.insert(transaction);
            log.info("交易记录创建成功: contractId={}, transactionId={}", 
                    contract.getContractId(), transaction.getTransactionId());
        } catch (Exception e) {
            log.error("创建交易记录失败: contractId={}", contract.getContractId(), e);
            // 不抛异常，避免影响合同审核流程
        }
    }
}
