package com.atguigu.gmall.pms.controller;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 商品三级分类
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 18:14:15
 */
@Api(tags = "商品三级分类 管理")
@RestController
@RequestMapping("pms/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @ApiOperation("根据三级级分类id查询一二三级分类集合")
    @GetMapping("all/{id}")
    public  ResponseVo<List<CategoryEntity>> queryLevel123CategoriesByCid3(@PathVariable Long id){
        List<CategoryEntity> categoryEntities =categoryService.queryLevel123CategoriesByCid3(id);
        return ResponseVo.ok(categoryEntities);
    }

    @ApiOperation("根据一级分类id查询二三级分类集合")
    @GetMapping("lv2/subs/{pid}")
    public ResponseVo<List<CategoryEntity>> getSubsCategories(@PathVariable Long pid) {
        List<CategoryEntity> categoryEntities = categoryService.getSubsCategories(pid);
//        String json = stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        //防止缓存击穿,添加分布式锁
//        RLock lock = redissonClient.getLock("pms:lock:" + pid);
//        lock.lock();
//        if(StringUtils.isNotBlank(json)){
//            return ResponseVo.ok(JSON.parseArray(json,CategoryEntity.class));
//        }
//        List<CategoryEntity> categoryEntities=categoryService.getlv2WithSubsCategories(pid);
//        if(CollectionUtils.isEmpty(categoryEntities)){
//            //防止缓存穿透
//            stringRedisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryEntities),300, TimeUnit.SECONDS);
//        }else{
//            //防止缓存雪崩
//        stringRedisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryEntities),300+new Random().nextInt(10), TimeUnit.SECONDS);
//        }
//        lock.unlock();
        return ResponseVo.ok(categoryEntities);
    }


    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryCategoryByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = categoryService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }

    @GetMapping("parent/{id}")
    @ApiOperation("查询父分类下子分类")
    public ResponseVo<List<CategoryEntity>> queryCategoryListByPid(@PathVariable("id") Long id) {
        List<CategoryEntity> list = categoryService.queryCategoryListByPid(id);
        return ResponseVo.ok(list);
    }

    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id) {
        CategoryEntity category = categoryService.getById(id);

        return ResponseVo.ok(category);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody CategoryEntity category) {
        categoryService.updateById(category);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        categoryService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
