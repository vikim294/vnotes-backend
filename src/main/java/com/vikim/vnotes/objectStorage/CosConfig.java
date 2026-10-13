package com.vikim.vnotes.objectStorage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CosConfig {

    private final CosProperties properties;

    @Bean(destroyMethod = "shutdown")
    public COSClient cosClient(){
        COSCredentials credentials = new BasicCOSCredentials(
                        properties.secretId(),
                        properties.secretKey()
                );

        Region region = new Region(properties.region());

        ClientConfig clientConfig = new ClientConfig(region);

        return new COSClient(
                credentials,
                clientConfig
        );
    }
}
