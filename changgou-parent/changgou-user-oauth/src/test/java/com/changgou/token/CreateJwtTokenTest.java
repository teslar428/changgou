package com.changgou.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.token
 ****/
public class CreateJwtTokenTest {

    /***
     * 创建令牌测试
     */
    @Test
    public void testCreateToken(){
        //1.访问证书路径（导出证书）
        ClassPathResource resource = new ClassPathResource("changgou65.jks");

        //2.创建秘钥工厂(加载证书信息)
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,"changgou65".toCharArray());

        //读取秘钥对(公钥、私钥)
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgou65","changgou65".toCharArray());

        //获取私钥
        RSAPrivateKey rsaPrivate = (RSAPrivateKey) keyPair.getPrivate();

        //定义Payload
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", "1");
        tokenMap.put("name", "oauth");
        tokenMap.put("authorities", new String[]{"admin","user"});
        //生成一个令牌，角色为admin

        //生成Jwt令牌(此处将私钥作为了秘钥)
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(rsaPrivate));

        //取出令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }


    /***
     * 解密令牌信息
     */
    @Test
    public void testParseToken(){
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgz7VZ7VuiHMCJ33QkIldVkFfDJhgyYLVaJTRjBYcOPkYeAwgNadSjDPEl6WdVWzWTzglkOyy345qNTiXGCpbfRUX94aHfn/Wi/XvoLhRdGSIhSFmMlH3jdY1Y1O+V25QMReSSfw6jjXIRYdVzIZpTnz7FQAVjRMSy000u/LiKrJp6ioF/tJ4oYh0r3BIPQL8zONCldJQbl5JSrTy5QS8lpUB2G5Sex0grWHowtNeQwddeEfRP0SWPakXvWo9V2WEJE1EKMuJrl5bUDMNUAo3rJwql7X7SdMCc524FAecCHB2QcVxc1djTcuTLFCbygpxCTc5718qxgMjVLx1jbanawIDAQAB-----END PUBLIC KEY-----";
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoib2F1dGgiLCJpZCI6IjEiLCJhdXRob3JpdGllcyI6WyJhZG1pbiJdfQ.QrUsK3_glC3ZZNwQRjY7lLczkLy-YDCC3saMSSvzdeQ-O92LNZGeJ5H-s7RQ7jqsqDv56fT4-YMOxPkX0DGK1hRBtAHO9OTH35P6rQ4FTMKDcXuAo-qlsYk0u15q15MXDZknjI8wOoqIubao-XrmwRgsI3OYKizp9wD68IRK3PNIgZLj7w5fokcoZAz9zDO3Iplu79lkLQD5AW6G4hyq2nIn95fa0A5Utm6u-gtdbS1upiCfUGJCpyS2FBrdDFWBiDC0J1ZS994R5i061j3m3ehgH0hVrXXyxtBEiEmwE9w3DUCbW7cRqX4S5H2U0Xb3bQ6oV0iGxZbMX7ew3-5HGA";
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));
        String claims = jwt.getClaims();
        System.out.println(claims);
    }

}
