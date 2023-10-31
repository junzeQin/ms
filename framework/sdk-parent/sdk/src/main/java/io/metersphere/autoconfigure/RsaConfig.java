package io.metersphere.autoconfigure;

import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import io.metersphere.service.FileService;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.annotation.Resource;

import static io.metersphere.commons.constants.CommonConstants.METER_SPHERE_RSA_KEY;
import static io.metersphere.commons.constants.CommonConstants.METER_SPHERE_RSA_PRIVATE_KEY;
import static io.metersphere.commons.constants.CommonConstants.METER_SPHERE_RSA_PUBLIC_KEY;

public class RsaConfig implements ApplicationRunner {

    @Resource
    private FileService fileService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        RsaKey value = fileService.checkRsaKey();
        // 保存到缓存中
        RsaUtil.setRsaKey(value);
        stringRedisTemplate.opsForHash().put(METER_SPHERE_RSA_KEY, METER_SPHERE_RSA_PUBLIC_KEY, value.getPublicKey());
        stringRedisTemplate.opsForHash().put(METER_SPHERE_RSA_KEY, METER_SPHERE_RSA_PRIVATE_KEY, value.getPrivateKey());
    }
}
