package com.atguigu.gmall.job.jobhandler;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.job.mapper.CartMapper;
import com.atguigu.gmall.job.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@SuppressWarnings("all")
public class MyJobHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Autowired
    private CartMapper cartMapper;



    private static final String EXCEPTION_KEY = "gmall:cart:async:exception:userId";
    private static final String KEY = "gmall:cart:";
    @XxlJob("cartDataSyncJobHandler")
    public ReturnT<String> dataSync(String param){

        // 读取redis中失败用户的信息
        BoundSetOperations<String, String> setOps = this.redisTemplate.boundSetOps(EXCEPTION_KEY);

        // 随机获取并移除一个用户

        String userId = null;
        try {
            userId = setOps.pop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(userId);
        while (userId != null) {
            System.out.println("userId="+userId);
            // 全部删除失败用户的mysql中的购物车

            this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId));

            // 读取redis中的失败用户的所有购物车记录，如果redis中没有对应用户的购物车直接结束
            if (!this.redisTemplate.hasKey(KEY+userId)) {
                return ReturnT.SUCCESS;
            }

            BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY+userId);
            List<Object> cartJsons = hashOps.values();
            cartJsons.forEach(cartJson -> {
                // 新增redis中对应的购物车记录
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                this.cartMapper.insert(cart);
            });

            userId = setOps.pop();
        }

        return ReturnT.SUCCESS;
    }

    /**
     * 1.方法必须有ReturnT<String>返回值，必须有一个String类型的形参
     * 2.通过@XxlJob("任务的唯一标识")声明该方法是一个定时任务
     * 3.如果想向调度中心输出日志，应该使用XxlJobLogger.log方法
     * @param param
     * @return
     */
    @XxlJob("myJobHandler")
    public ReturnT<String> test(String param){
        System.out.println("任务执行时间：" +new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())) +"\n参数是:" + param);
        XxlJobLogger.log("MyJobHandler executed " + param);

        return ReturnT.SUCCESS;
    }
}
