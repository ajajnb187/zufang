package org.example.rentingmanagement.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PDF生成和数字签名工具类
 * 基于iText5实现电子合同PDF生成和SHA256哈希防篡改
 */
@Slf4j
public class PDFSignUtil {

    private static final String FONT_PATH = "STSong-Light";
    private static final String ENCODING = "UniGB-UCS2-H";

    /**
     * 生成租赁合同PDF
     * @param contractData 合同数据
     * @param outputStream 输出流
     */
    public static void generateContractPDF(ContractData contractData, OutputStream outputStream) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // 设置中文字体
        BaseFont baseFont = BaseFont.createFont(FONT_PATH, ENCODING, BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(baseFont, 20, Font.BOLD);
        Font contentFont = new Font(baseFont, 12, Font.NORMAL);
        Font boldFont = new Font(baseFont, 12, Font.BOLD);
        
        // 标题
        Paragraph title = new Paragraph("房屋租赁合同", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // 合同编号
        Paragraph contractNo = new Paragraph("合同编号：" + contractData.getContractNo(), contentFont);
        contractNo.setAlignment(Element.ALIGN_CENTER);
        contractNo.setSpacingAfter(20);
        document.add(contractNo);
        
        // 甲方（房东）信息
        document.add(new Paragraph("甲方（出租方）：" + contractData.getLandlordName(), boldFont));
        document.add(new Paragraph("联系电话：" + contractData.getLandlordPhone(), contentFont));
        document.add(new Paragraph("", contentFont));
        
        // 乙方（租客）信息
        document.add(new Paragraph("乙方（承租方）：" + contractData.getTenantName(), boldFont));
        document.add(new Paragraph("联系电话：" + contractData.getTenantPhone(), contentFont));
        document.add(new Paragraph("", contentFont));
        
        // 第一条 房屋基本信息
        document.add(new Paragraph("第一条 房屋基本信息", boldFont));
        document.add(new Paragraph("房屋地址：" + contractData.getHouseAddress(), contentFont));
        document.add(new Paragraph("房屋面积：" + contractData.getHouseArea() + "平方米", contentFont));
        document.add(new Paragraph("房屋户型：" + contractData.getHouseType(), contentFont));
        document.add(new Paragraph("", contentFont));
        
        // 第二条 租赁期限
        document.add(new Paragraph("第二条 租赁期限", boldFont));
        document.add(new Paragraph("租赁期限自 " + contractData.getStartDate() + " 至 " + contractData.getEndDate() + " 止。", contentFont));
        document.add(new Paragraph("", contentFont));
        
        // 第三条 租金及支付方式
        document.add(new Paragraph("第三条 租金及支付方式", boldFont));
        document.add(new Paragraph("月租金：人民币 " + contractData.getRentPrice() + " 元", contentFont));
        document.add(new Paragraph("支付方式：" + contractData.getPaymentMethod(), contentFont));
        document.add(new Paragraph("", contentFont));
        
        // 第四条 双方权利义务
        document.add(new Paragraph("第四条 双方权利义务", boldFont));
        document.add(new Paragraph("甲方权利义务：", boldFont));
        document.add(new Paragraph("1. 保证房屋符合居住条件，房屋设施完好", contentFont));
        document.add(new Paragraph("2. 及时维修房屋及设施", contentFont));
        document.add(new Paragraph("3. 不得随意进入租赁房屋", contentFont));
        document.add(new Paragraph("", contentFont));
        
        document.add(new Paragraph("乙方权利义务：", boldFont));
        document.add(new Paragraph("1. 按时支付租金", contentFont));
        document.add(new Paragraph("2. 爱护房屋及设施，合理使用", contentFont));
        document.add(new Paragraph("3. 不得擅自转租、改变房屋用途", contentFont));
        document.add(new Paragraph("", contentFont));
        
        // 第五条 违约责任
        document.add(new Paragraph("第五条 违约责任", boldFont));
        document.add(new Paragraph("任何一方违约，应承担相应的违约责任。", contentFont));
        document.add(new Paragraph("", contentFont));
        
        // 第六条 其他约定
        if (contractData.getCustomContent() != null && !contractData.getCustomContent().isEmpty()) {
            document.add(new Paragraph("第六条 其他约定", boldFont));
            document.add(new Paragraph(contractData.getCustomContent(), contentFont));
            document.add(new Paragraph("", contentFont));
        }
        
        // 签名区域
        document.add(new Paragraph("", contentFont));
        document.add(new Paragraph("", contentFont));
        
        // 创建签名表格
        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);
        signTable.setSpacingBefore(30);
        
        PdfPCell landlordCell = new PdfPCell();
        landlordCell.setBorder(Rectangle.NO_BORDER);
        Paragraph landlordSign = new Paragraph("甲方签名：", contentFont);
        landlordCell.addElement(landlordSign);
        if (contractData.getLandlordSignImage() != null) {
            try {
                Image signImage = Image.getInstance(contractData.getLandlordSignImage());
                signImage.scaleToFit(100, 50);
                landlordCell.addElement(signImage);
            } catch (Exception e) {
                log.error("加载房东签名图片失败", e);
            }
        }
        signTable.addCell(landlordCell);
        
        PdfPCell tenantCell = new PdfPCell();
        tenantCell.setBorder(Rectangle.NO_BORDER);
        Paragraph tenantSign = new Paragraph("乙方签名：", contentFont);
        tenantCell.addElement(tenantSign);
        if (contractData.getTenantSignImage() != null) {
            try {
                Image signImage = Image.getInstance(contractData.getTenantSignImage());
                signImage.scaleToFit(100, 50);
                tenantCell.addElement(signImage);
            } catch (Exception e) {
                log.error("加载租客签名图片失败", e);
            }
        }
        signTable.addCell(tenantCell);
        
        document.add(signTable);
        
        // 签署日期
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        Paragraph signDate = new Paragraph("签署日期：" + currentDate, contentFont);
        signDate.setAlignment(Element.ALIGN_CENTER);
        signDate.setSpacingBefore(20);
        document.add(signDate);
        
        document.close();
        writer.close();
    }

    /**
     * 计算文件的SHA256哈希值
     * @param fileBytes 文件字节数组
     * @return SHA256哈希值（十六进制字符串）
     */
    public static String calculateSHA256(byte[] fileBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("计算SHA256哈希值失败", e);
            throw new RuntimeException("计算哈希值失败", e);
        }
    }

    /**
     * 验证文件完整性
     * @param fileBytes 文件字节数组
     * @param originalHash 原始哈希值
     * @return 是否完整
     */
    public static boolean verifyIntegrity(byte[] fileBytes, String originalHash) {
        String currentHash = calculateSHA256(fileBytes);
        return currentHash.equalsIgnoreCase(originalHash);
    }

    /**
     * 合同数据类
     */
    public static class ContractData {
        private String contractNo;
        private String landlordName;
        private String landlordPhone;
        private String tenantName;
        private String tenantPhone;
        private String houseAddress;
        private Double houseArea;
        private String houseType;
        private String startDate;
        private String endDate;
        private Double rentPrice;
        private String paymentMethod;
        private String customContent;
        private String landlordSignImage;
        private String tenantSignImage;

        // Getters and Setters
        public String getContractNo() { return contractNo; }
        public void setContractNo(String contractNo) { this.contractNo = contractNo; }
        
        public String getLandlordName() { return landlordName; }
        public void setLandlordName(String landlordName) { this.landlordName = landlordName; }
        
        public String getLandlordPhone() { return landlordPhone; }
        public void setLandlordPhone(String landlordPhone) { this.landlordPhone = landlordPhone; }
        
        public String getTenantName() { return tenantName; }
        public void setTenantName(String tenantName) { this.tenantName = tenantName; }
        
        public String getTenantPhone() { return tenantPhone; }
        public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }
        
        public String getHouseAddress() { return houseAddress; }
        public void setHouseAddress(String houseAddress) { this.houseAddress = houseAddress; }
        
        public Double getHouseArea() { return houseArea; }
        public void setHouseArea(Double houseArea) { this.houseArea = houseArea; }
        
        public String getHouseType() { return houseType; }
        public void setHouseType(String houseType) { this.houseType = houseType; }
        
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        
        public Double getRentPrice() { return rentPrice; }
        public void setRentPrice(Double rentPrice) { this.rentPrice = rentPrice; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getCustomContent() { return customContent; }
        public void setCustomContent(String customContent) { this.customContent = customContent; }
        
        public String getLandlordSignImage() { return landlordSignImage; }
        public void setLandlordSignImage(String landlordSignImage) { this.landlordSignImage = landlordSignImage; }
        
        public String getTenantSignImage() { return tenantSignImage; }
        public void setTenantSignImage(String tenantSignImage) { this.tenantSignImage = tenantSignImage; }
    }
}
