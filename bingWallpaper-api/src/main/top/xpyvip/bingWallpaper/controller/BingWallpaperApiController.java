package top.xpyvip.bingWallpaper.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import top.xpyvip.bingWallpaper.domain.BingWallpaperInfo;
import top.xpyvip.bingWallpaper.jobs.BingWallpaperAutoAcquireJob;
import top.xpyvip.bingWallpaper.services.IBingWallpaperInfoService;

import javax.annotation.Resource;

@Slf4j
@Validated
@RequiredArgsConstructor
@Controller
@RequestMapping("/image")
public class BingWallpaperApiController {

    @Resource
    private IBingWallpaperInfoService iBingWallpaperInfoService;

    @Resource
    private BingWallpaperAutoAcquireJob bingWallpaperAutoAcquireJob;

    /**
     * 返回当天1080p的图像
     *
     */
    @GetMapping()
    public String get() throws Exception {
        LambdaQueryWrapper<BingWallpaperInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(BingWallpaperInfo::getStartTime,DateUtil.format(DateUtil.date(), DatePattern.NORM_DATE_FORMAT));
        BingWallpaperInfo bingWallpaperInfo = iBingWallpaperInfoService.getOne(queryWrapper);
        if(ObjUtil.isEmpty(bingWallpaperInfo)){
            // 查询数据库中当前最大值
            queryWrapper.orderByDesc(BingWallpaperInfo::getStartTime).last("limit 1");
            bingWallpaperInfo = iBingWallpaperInfoService.getOne(queryWrapper);
        }
        // 获取当前最大值图像
        return "redirect:https://cn.bing.com" + getImageUrl(bingWallpaperInfo, null);
    }

    /**
     * 根据分辨率返回对应分辨率图片url
     * w=1920x1080 表示输出1080P分辨率图像，w=1600 表示自定义宽度为1600px的图像
     * 固定分辨率支持如下： UHD , 1920x1200 , 1920x1080 , 1366x768 , 1280x768 ,1024x768 , 800x600 , 800x480 , 768x1280 ,
     * 720x1280 , 640x480 , 480x800 , 400x240 , 320x240 , 240x320
     * 其他说明： w参数可以去掉，默认输出当天1920x1080分辨率图片
     * @param bingWallpaperInfo
     * @param w
     * @return
     */
    private String getImageUrl(BingWallpaperInfo bingWallpaperInfo, String w) {
        if(ObjUtil.isEmpty(bingWallpaperInfo)){
            return "";
        }
        if(StrUtil.isEmpty(w)) {
            w = "1920x1080";
        }
        if(!w.contains("x")) {
            return bingWallpaperInfo.getUrlbase() + "_UHD.jpg&w=" + w;
        }
        return bingWallpaperInfo.getUrlbase() + "_" + w + ".jpg";
    }
}
