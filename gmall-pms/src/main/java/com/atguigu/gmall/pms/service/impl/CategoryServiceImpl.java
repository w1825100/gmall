package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.aspect.GmallCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
@SuppressWarnings("all")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {
    private static final String  KEY_PREFIX="gmall:pms:cates:";
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<>()
        );

        return new PageResultVo(page);
    }

    //使用aop注解缓存
    @GmallCache(prefix = KEY_PREFIX,timeout = 300,random = 300,lock="gmall:pms:cates:lock:")
    @Override
    public List<CategoryEntity> getSubsCategories(Long pid) {
        return  categoryMapper.getlv2WithSubsCategories(pid);
    }

    @Override
    public List<CategoryEntity> queryCategoryListByPid(Long id) {
        QueryWrapper<CategoryEntity> queryWrapper=new QueryWrapper();
        if(id!=-1){
            queryWrapper.eq("parent_id",id);
        }
        List list=baseMapper.selectList(queryWrapper);
        return list;
    }

    @Override
    public List<CategoryEntity> queryLevel123CategoriesByCid3(Long id) {
        CategoryEntity cateThird = this.getById(id);
        if(cateThird==null){
            return null;
        }
        CategoryEntity cateTwo = this.getById(cateThird.getParentId());
        CategoryEntity cateOne = this.getById(cateTwo.getParentId());

        return  Arrays.asList(cateOne,cateTwo,cateThird);
    }

}
