package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.expection.GmallException;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@SuppressWarnings("all")
@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    WareSkuMapper wareSkuMapper;
    @Autowired
    RabbitTemplate rabbitTemplate;

    private static final String LOCK_PREFIX = "gmall:stock:lock";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    @Transactional
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos, String orderToken) {
        log.warn("wms服务开始检查库存,:{}",lockVos);
        if (CollectionUtils.isEmpty(lockVos)) {
            throw new GmallException("购物车没有选中的商品");
        }

        //分布式锁,锁库存
        lockVos.forEach(lockVo -> {
            lockStore(lockVo);
        });

        //判断所有商品库存锁定情况
       if(lockVos.stream().anyMatch(lockVo->!lockVo.getLock())){
            List<SkuLockVo> successLockVos = lockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList());
            // 遍历所有锁定成功的商品解锁库存
            if (!CollectionUtils.isEmpty(successLockVos)){
                successLockVos.forEach(lockVo -> this.wareSkuMapper.unlock(lockVo.getWareSkuId(), lockVo.getCount()));
            }
            log.warn("wms锁定库存失败:{}",lockVos);
            // 有商品锁定失败的情况下，需要把锁定信息返回给消费方
            return lockVos;
        }
        // 所有商品锁定成功的情况下，返回之前，应该把锁定信息缓存到redis中，以方便将来的某个时刻解锁对应的库存
        this.redisTemplate.opsForValue().set(LOCK_PREFIX + orderToken, JSON.toJSONString(lockVos));

        // 所有商品锁定成功之后，发送消息，定时解锁库存
        this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.ttl", orderToken);
        log.warn("wms微服务锁定库存成功,发送消息到延时队列,100秒后自动解锁库存orderToken:{}",orderToken);
    // 如果所有商品都锁定成功，返回null
        return null;
    }

    private void lockStore(SkuLockVo lockVo) {
        RLock lock = null;
        try {
            //分布式锁,锁库存
            Long skuId = lockVo.getSkuId();
            Integer count = lockVo.getCount();
             lock = redissonClient.getFairLock(LOCK_PREFIX + skuId);
             lock.lock();
             log.warn("lockName,{},剩余时间:{}",lock.getName(),lock.remainTimeToLive());
            List<WareSkuEntity> wareSkuEntities = this.wareSkuMapper.check(skuId, count);
            if (CollectionUtils.isEmpty(wareSkuEntities)){
                lockVo.setLock(false);
                return;
            }

            // 锁库存：更新库存表中的stock_lock字段。大数据提供库存接口，获取就近的库存id。这里取第一个库存。
            Long id = wareSkuEntities.get(0).getId();
            if (this.wareSkuMapper.lock(id, lockVo.getCount()) == 1) {
                lockVo.setLock(true);
                lockVo.setWareSkuId(id);
                return;
            }
        }  finally {
            lock.unlock();
        }
    }

}
