package com.changgou.oauth.config;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.oauth.config
 ****/
public class JwtToken {

    /***
     * 颁发管理员令牌
     * @return
     */
    public static String adminJwt() {
        //1.访问证书路径（导出证书）
        ClassPathResource resource = new ClassPathResource("changgou65.jks");

        //2.创建秘钥工厂(加载证书信息)
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, "changgou65".toCharArray());

        //读取秘钥对(公钥、私钥)
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgou65", "changgou65".toCharArray());

        //获取私钥
        RSAPrivateKey rsaPrivate = (RSAPrivateKey) keyPair.getPrivate();

        //定义Payload
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", "1");
        tokenMap.put("name", "oauth");
        tokenMap.put("authorities", new String[]{"admin"});
        //生成一个令牌，角色为admin

        //生成Jwt令牌(此处将私钥作为了秘钥)
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(rsaPrivate));

        //取出令牌
        String encoded = jwt.getEncoded();
        return encoded;
    }
}
