package com.gurula.stockMate.oauth;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.gurula.stockMate.exception.InvalidTokenException;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTool {
    private final JWTSigner jwtSigner;

    public JwtTool(KeyPair keyPair) {
        this.jwtSigner = JWTSignerUtil.createSigner("rs256", keyPair);
    }

    /**
     * 创建 access-token
     * @param memberId
     * @param ttl
     * @return
     */
    public String createToken(String memberId, Duration ttl) {
        // 1.生成jws
        return JWT.create()
                .setPayload("member", memberId)
                .setExpiresAt(new Date(System.currentTimeMillis() + ttl.toMillis()))
                .setSigner(jwtSigner)
                .sign();
    }

    /**
     * 解析token
     *
     * @param token token
     * @return 解析token得到的使用者資訊
     */
    public String parseToken(String token) {
        // 1.檢驗token是否為空值
        if (token == null) {
            System.out.println("Token 未提供");
            throw new InvalidTokenException("Token 未提供");
        }
        // 2.檢驗並解析JWT
        JWT jwt;
        try {
            jwt = JWT.of(token).setSigner(jwtSigner);
        } catch (Exception e) {
            System.out.println("無效的 Token 格式或簽名錯誤");
            throw new InvalidTokenException("無效的 Token 格式或簽名錯誤");
        }
        // 2.檢驗JWT是否有效
        if (!jwt.verify()) {
            // 驗證失敗
            System.out.println("Token 驗證失敗");
            throw new InvalidTokenException("Token 驗證失敗");
        }
        // 3.檢驗是否過期
        try {
            JWTValidator.of(jwt).validateDate();
        } catch (ValidateException e) {
            System.out.println("Token 已過期");
            throw new InvalidTokenException("Token 已過期");
        }
        // 4.data格式檢查
        Object userPayload = jwt.getPayload("member");
        if (userPayload == null) {
            // data為空值
            System.out.println("Token 資料為空");
            throw new InvalidTokenException("Token 資料為空");
        }
        // 5.data解析
        try {
           return userPayload.toString();
        } catch (RuntimeException e) {
            // data格式有誤
            System.out.println("Token 解析失敗");
            throw new InvalidTokenException("Token 解析失敗");
        }
    }
}