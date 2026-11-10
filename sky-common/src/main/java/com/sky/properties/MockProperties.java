package com.sky.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 开启mock的配置类
 */
@Component
@ConfigurationProperties(prefix = "sky.mock")
@Data
public class MockProperties {

    // 支付功能是否mock
    private boolean payMock;
}
