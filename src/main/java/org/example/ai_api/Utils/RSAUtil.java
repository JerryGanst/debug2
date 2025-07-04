package org.example.ai_api.Utils;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.crypto.Cipher;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSAUtil {
    public static final int KEY_SIZE = 2048;

    /**
     * 生成密钥对
     * @return RSA密钥对
     * @throws Exception 过程中出现的异常
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(KEY_SIZE);
        return generator.generateKeyPair();
    }

    /**
     * 将二进制公钥转换为PEM格式(方便存储)
     * @param publicKey 二进制公钥
     * @return PEM格式公钥
     * @throws Exception 过程中出现的异常
     */
    public static String convertToPem(PublicKey publicKey) throws Exception {
        StringWriter stringWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(stringWriter)) {
            pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
        }
        return stringWriter.toString();
    }

    /**
     * 解密使用RSA公钥加密的数据
     * @param encryptedBase64 Base64编码的加密数据
     * @param privateKey 配对的私钥对象，需与加密公钥对应
     * @return 解密后的原始UTF-8字符串
     * @throws Exception 过程中出现的异常
     */
    public static String decrypt(String encryptedBase64, PrivateKey privateKey) throws Exception {
        byte[] data = Base64.getDecoder().decode(encryptedBase64);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
    }
}
