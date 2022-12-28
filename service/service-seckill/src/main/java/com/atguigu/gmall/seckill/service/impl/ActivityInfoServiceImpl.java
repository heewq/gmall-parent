package com.atguigu.gmall.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.seckill.entity.ActivityInfo;
import com.atguigu.gmall.seckill.service.ActivityInfoService;
import com.atguigu.gmall.seckill.mapper.ActivityInfoMapper;
import org.springframework.stereotype.Service;

/**
 * @author vcwfhe
 * @description 针对表【activity_info(活动表)】的数据库操作Service实现
 * @createDate 2022-12-27 20:16:44
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo>
        implements ActivityInfoService {

}
