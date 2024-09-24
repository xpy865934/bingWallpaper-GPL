package top.xpyvip.bingWallpaper.services.impl;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import top.xpyvip.bingWallpaper.domain.SystemInfo;
import top.xpyvip.bingWallpaper.mapper.SystemInfoMapper;
import top.xpyvip.bingWallpaper.services.ISystemInfoService;
import top.xpyvip.bingWallpaper.utils.redis.RedisUtils;

/**
 * 系统信息
 */
@RequiredArgsConstructor
@Service
public class SystemInfoServiceImpl extends ServiceImpl<SystemInfoMapper, SystemInfo> implements ISystemInfoService {

    @EventListener(classes = ApplicationReadyEvent.class)
    public void initData(){
        SystemInfo systemInfo = this.getOne(new QueryWrapper<>());
        if(ObjUtil.isNotEmpty(systemInfo)) {
            RedisUtils.setAtomicValue("visitCount", systemInfo.getVisitCount());
        }
    }
}
