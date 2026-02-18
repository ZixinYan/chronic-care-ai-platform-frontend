package com.zixin.utils.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 * 敏感数据加密处理器
 * 
 * 功能:
 * 1. 使用AES加密算法对敏感字段进行加密存储
 * 2. 查询时自动解密
 * 3. 保障数据安全性
 * 
 * 支持加密的字段类型:
 * - 手机号
 * - 邮箱
 * - 身份证号
 * - 其他敏感信息
 * 
 * 使用方式:
 * 在PO类的敏感字段上添加注解:
 * @TableField(typeHandler = SensitiveDataEncryptHandler.class)
 * private String phone;
 * 
 * 配置说明:
 * 需要在application.yml中配置加密密钥:
 * encryption:
 *   aes:
 *     key: ${ENCRYPTION_AES_KEY:ChronicCare2024!}
 *     iv: ${ENCRYPTION_AES_IV:ChronicCareIV24!}
 * 
 * @author zixin
 */
@Slf4j
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class SensitiveDataEncryptHandler extends BaseTypeHandler<String> {

    /**
     * AES加密算法
     */
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    
    /**
     * AES密钥(从配置文件读取)
     * 生产环境必须使用环境变量或配置中心管理
     */
    @Value("${encryption.aes.key}")
    private String secretKey;
    
    /**
     * AES初始化向量(从配置文件读取)
     */
    @Value("${encryption.aes.iv}")
    private String iv;

    /**
     * 设置非空参数
     * 写入数据库前加密
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.trim().isEmpty()) {
            ps.setString(i, null);
            return;
        }
        
        try {
            String encryptedValue = encrypt(parameter);
            ps.setString(i, encryptedValue);
        } catch (Exception e) {
            log.error("Failed to encrypt sensitive data", e);
            throw new SQLException("Encryption failed", e);
        }
    }

    /**
     * 获取可为空的结果
     * 从数据库读取后解密
     */
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String encryptedValue = rs.getString(columnName);
        return decrypt(encryptedValue);
    }

    /**
     * 获取可为空的结果(通过列索引)
     */
    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String encryptedValue = rs.getString(columnIndex);
        return decrypt(encryptedValue);
    }

    /**
     * 获取可为空的结果(存储过程)
     */
    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String encryptedValue = cs.getString(columnIndex);
        return decrypt(encryptedValue);
    }

    /**
     * AES加密
     *
     * @param plainText 明文
     * @return Base64编码的密文
     */
    private String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.trim().isEmpty()) {
            return plainText;
        }

        // 创建密钥
        SecretKeySpec keySpec = new SecretKeySpec(
                adjustKeyLength(secretKey).getBytes(StandardCharsets.UTF_8), 
                "AES"
        );
        
        // 创建初始化向量
        IvParameterSpec ivSpec = new IvParameterSpec(
                adjustIvLength(iv).getBytes(StandardCharsets.UTF_8)
        );

        // 初始化加密器
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        // 加密
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        
        // Base64编码
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES解密
     *
     * @param encryptedText Base64编码的密文
     * @return 明文
     */
    private String decrypt(String encryptedText) throws SQLException {
        if (encryptedText == null || encryptedText.trim().isEmpty()) {
            return encryptedText;
        }

        try {
            // Base64解码
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

            // 创建密钥
            SecretKeySpec keySpec = new SecretKeySpec(
                    adjustKeyLength(secretKey).getBytes(StandardCharsets.UTF_8), 
                    "AES"
            );
            
            // 创建初始化向量
            IvParameterSpec ivSpec = new IvParameterSpec(
                    adjustIvLength(iv).getBytes(StandardCharsets.UTF_8)
            );

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 解密
            byte[] decrypted = cipher.doFinal(encryptedBytes);
            
            return new String(decrypted, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Failed to decrypt sensitive data: {}", encryptedText, e);
            // 解密失败时返回null，避免暴露原始数据
            return null;
        }
    }

    /**
     * 调整密钥长度为16字节(AES-128)
     * 
     * @param key 原始密钥
     * @return 16字节密钥
     */
    private String adjustKeyLength(String key) {
        if (key == null) {
            key = "ChronicCare2024!";
        }
        
        if (key.length() > 16) {
            return key.substring(0, 16);
        } else if (key.length() < 16) {
            // 使用重复填充
            StringBuilder sb = new StringBuilder(key);
            while (sb.length() < 16) {
                sb.append(key);
            }
            return sb.substring(0, 16);
        }
        
        return key;
    }

    /**
     * 调整IV长度为16字节
     * 
     * @param ivValue 原始IV
     * @return 16字节IV
     */
    private String adjustIvLength(String ivValue) {
        if (ivValue == null) {
            ivValue = "ChronicCareIV24!";
        }
        
        if (ivValue.length() > 16) {
            return ivValue.substring(0, 16);
        } else if (ivValue.length() < 16) {
            StringBuilder sb = new StringBuilder(ivValue);
            while (sb.length() < 16) {
                sb.append(ivValue);
            }
            return sb.substring(0, 16);
        }
        
        return ivValue;
    }
}
