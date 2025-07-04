package org.example.ai_api.Service;

import org.example.ai_api.Bean.Entity.RSAKeyPair;
import org.example.ai_api.Exception.DataNotComplianceException;
import org.example.ai_api.Exception.NotFoundException;
import org.example.ai_api.Repository.RSAKeyPairRepository;
import org.example.ai_api.Utils.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;

@Service
public class RSAKeyService {
    @Autowired
    private RSAKeyPairRepository rsaKeyRepository;

    /**
     * 生成并保存新的密钥对
     * @return 密钥对
     * @throws Exception 操作过程中可能存在的异常
     */
    public RSAKeyPair generateRSAKeyPair() throws Exception {
        KeyPair keyPair = RSAUtil.generateKeyPair();
        RSAKeyPair document = new RSAKeyPair();
        document.setPublicKey(RSAUtil.convertToPem(keyPair.getPublic()));
        document.setPrivateKey(keyPair.getPrivate().getEncoded());
        return rsaKeyRepository.save(document);
    }

    /**
     * 根据requestId获取私钥
     * @param requestId 登录请求的id
     * @return 私钥
     * @throws Exception 操作过程中可能存在的异常
     */
    public PrivateKey getPrivateKeyByRequestId(String requestId) throws Exception {
        RSAKeyPair document = rsaKeyRepository.findByRequestId(requestId);
        if (document == null) {
            throw new NotFoundException("未找到密钥对");
        }
        if (document.getExpiresAt().isBefore(Instant.now())) {
            throw new DataNotComplianceException("密钥已过期");
        }
        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(document.getPrivateKey()));
    }
}
