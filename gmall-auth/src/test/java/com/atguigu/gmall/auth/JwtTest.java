package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "D:\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");

    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);

    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MTM3MTE1MzJ9.QZQtZldhvB3vxh9fP1yWhcYxOK1fE0BfVLCCGUMMcAFKj4QgKmzSSMTOkoLDuRLW0CmSMRj3Jxp2nUSkmmYe5h0xtRr3MvbN2hFYJtJiMUKleqTHEhdAKZxX1qxRTsa3YxaGJ9lAnwI5X7EAJlGzW0weRhSzdrPoYS3jPT1nEsQvb0FHi42CVtjPemG6IExlJ1aLtJwank2HAvpSnvVSX1X9UWPwu-BaJJYeTpqeJNNaftYYpxHAED1666dY27u8uVuEevxGdXwuvR7BkhMC0Q_CzO0ci7RylAi4utkdTi7Nid1h-CnozhUT4J4Y16S2hrVH1I7oC83Pk5jZjMVoUw";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
