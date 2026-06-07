package com.haduy.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.config.import=",
        "jwt.secret=dGVzdC1vbmx5LXNlY3JldC1mb3ItdW5pdC10ZXN0cy1ub3QtZm9yLXByb2R1Y3Rpb24tYXQtYWxs"
})
class EcommerceApplicationTests {

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }
}
