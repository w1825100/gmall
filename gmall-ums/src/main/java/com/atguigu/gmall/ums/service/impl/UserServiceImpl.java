package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.common.expection.GmallException;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.CollectionUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> qr = new QueryWrapper<>();
        switch(type){
            case 1:
                qr.eq("username",data);
                break;
            case 2:
                qr.eq("phone",data);
                break;
            case 3:
                qr.eq("email",data);
                break;
            default:
                return  null;
        }
        return this.count(qr)==0;
    }

    @Override
    public void code(String phone) {
        rabbitTemplate.convertAndSend("PMS_MSG_EXCHANGE","msg.send",phone);
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        String codeKey="mall:sms:"+userEntity.getPhone()+"code";
        String key = stringRedisTemplate.opsForValue().get(codeKey);
        if(key==null){
            throw new GmallException("验证码不正确");
        }
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        userEntity.setSalt(salt);
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword()+salt));
        userEntity.setLevelId(1L);
        userEntity.setNickname(userEntity.getUsername());
        userEntity.setSourceType(1);
        userEntity.setGrowth(1000);
        userEntity.setIntegration(1000);
        userEntity.setStatus(0);
        userEntity.setCreateTime(new Date());

        this.save(userEntity);
        stringRedisTemplate.delete(codeKey);
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        List<UserEntity> users = this.list(new QueryWrapper<UserEntity>().eq("username", loginName).or().eq("email", loginName).or().eq("phone", loginName));
       if(CollectionUtils.isEmpty(users)){
           return null;
       }

        for (UserEntity user : users) {
            password=DigestUtils.md5Hex(password+user.getSalt());
            if(StringUtils.equals(password,user.getPassword())){
                    return user;
            }
        }
        return null;
    }


}
