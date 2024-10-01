package top.xpyvip.bingWallpaper.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import top.xpyvip.bingWallpaper.domain.BingWallpaperInfo;
import top.xpyvip.bingWallpaper.domain.SystemInfo;
import top.xpyvip.bingWallpaper.services.IBingWallpaperInfoService;
import top.xpyvip.bingWallpaper.services.ISystemInfoService;
import top.xpyvip.bingWallpaper.utils.redis.RedisUtils;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ApplicationEventListener {

    @Resource
    private ISystemInfoService systemInfoService;

    @Resource
    private IBingWallpaperInfoService bingWallpaperInfoService;

    /**
     * 程序启动时加载访问量到redis数据库
     */
    @EventListener(classes = ApplicationReadyEvent.class)
    public void loadVisitCount(){
        SystemInfo systemInfo = systemInfoService.getOne(new QueryWrapper<>());
        if(ObjUtil.isNotEmpty(systemInfo)) {
            RedisUtils.setAtomicValue("visitCount", systemInfo.getVisitCount());
        }
    }

    /**
     * 程序启动时加载壁纸信息到redis数据库
     */
    @EventListener(classes = ApplicationReadyEvent.class)
    public void loadBingWallpaperInfo(){
        LambdaQueryWrapper<BingWallpaperInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(BingWallpaperInfo::getCreateTime);
        List<BingWallpaperInfo> list = bingWallpaperInfoService.list(queryWrapper);
        for (BingWallpaperInfo bingWallpaperInfo : list) {
            RedisUtils.setCacheObject(DateUtil.format(bingWallpaperInfo.getStartTime(), DatePattern.PURE_DATE_PATTERN), bingWallpaperInfo);
        }
        if(CollUtil.isNotEmpty(list)) {
            // 缓存最大值
            RedisUtils.setCacheObject("lastInfo", list.get(0));
        }
    }
}
