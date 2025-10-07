package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopConfiguration")
@RequestMapping("/user/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    private static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @ApiOperation("查询营业状态")
    @GetMapping("/status")
    public Result<Integer> getShopStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get("KEY");
        log.info("查询店铺营业状态：{}", status == 1? "营业中" : "打烊中");
        return Result.success(status);
    }
}