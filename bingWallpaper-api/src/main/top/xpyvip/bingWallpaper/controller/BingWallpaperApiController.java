package top.xpyvip.bingWallpaper.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import top.xpyvip.bingWallpaper.domain.BingWallpaperInfo;
import top.xpyvip.bingWallpaper.enums.ImageResolution;
import top.xpyvip.bingWallpaper.jobs.BingWallpaperAutoAcquireJob;
import top.xpyvip.bingWallpaper.services.IBingWallpaperInfoService;
import top.xpyvip.bingWallpaper.utils.ImageUtils;
import top.xpyvip.bingWallpaper.utils.redis.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @Resource
    private ImageUtils imageUtils;

    /**
     * 返回指定日期的图像， 默认如果day是空的，返回当天的图像，如果都不传，默认是当天1080p的图像
     *
     */
    @GetMapping()
    public void get(String day, String size, String width, String height, String type, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RedisUtils.incrAtomicValue("visitCount");
        DateTime now = DateUtil.date();
        try {
            if(StrUtil.isNotEmpty(day)) {
                now = DateUtil.parse(day, DatePattern.PURE_DATE_PATTERN);
            }
        } catch (Exception e) {
            log.info("格式化日期错误");
        }
        // 此处从redis查询，不直接从数据库查询
        BingWallpaperInfo bingWallpaperInfo = RedisUtils.getCacheObject(DateUtil.format(now, DatePattern.PURE_DATE_PATTERN));
        byte[] image = imageUtils.getImageUrl(bingWallpaperInfo, size, width, height, type);
        imageUtils.backImage(image, type, request, response);
    }

    /**
     * 返回指定日期的4K图像
     *
     */
    @GetMapping("/4K")
    public void get4K(String day, String type, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RedisUtils.incrAtomicValue("visitCount");
        DateTime now = DateUtil.date();
        try {
            if(StrUtil.isNotEmpty(day)) {
                now = DateUtil.parse(day, DatePattern.PURE_DATE_PATTERN);
            }
        } catch (Exception e) {
            log.info("格式化日期错误");
        }
        // 此处从redis查询，不直接从数据库查询
        BingWallpaperInfo bingWallpaperInfo = RedisUtils.getCacheObject(DateUtil.format(now, DatePattern.PURE_DATE_PATTERN));
        byte[] image = imageUtils.getImageUrl(bingWallpaperInfo, ImageResolution.UHD.getResolution(), null, null, type);
        imageUtils.backImage(image, type, request, response);
    }

    /**
     * 返回随机图像
     *
     */
    @GetMapping("/random")
    public void random(String width, String height, String type, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RedisUtils.incrAtomicValue("visitCount");
        // 计算 2022年4月27日距离当前是多少天
        long newImageDay = DateUtil.between(DateUtil.parse("2022-04-27", DatePattern.NORM_DATE_FORMAT), DateUtil.date(), DateUnit.DAY);
        long oldImageDay = DateUtil.between(DateUtil.parse("2010-01-01", DatePattern.NORM_DATE_FORMAT), DateUtil.date(), DateUnit.DAY);
        // 判断取新日期图片还是旧日期图片
        WeightRandom.WeightObj<Long>[] weightObjs = new WeightRandom.WeightObj[]{
                new WeightRandom.WeightObj<Long>(newImageDay, 7),
                new WeightRandom.WeightObj<Long>(oldImageDay, 3)
        };
        WeightRandom<Long> weightRandom = RandomUtil.weightRandom(weightObjs);
        Long next = weightRandom.next();
        DateTime dateTime = RandomUtil.randomDay(-next.intValue(), 0);
        // 此处从redis查询，不直接从数据库查询
        BingWallpaperInfo bingWallpaperInfo = RedisUtils.getCacheObject(DateUtil.format(dateTime, DatePattern.PURE_DATE_PATTERN));
        byte[] image = imageUtils.getImageUrl(bingWallpaperInfo, null, width, height, type);
        imageUtils.backImage(image, type, request, response);
    }

    /**
     * 更新缓存
     *
     */
    @GetMapping("/refresh")
    public void refreshCache(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RedisUtils.incrAtomicValue("visitCount");
        // 限制一个小时之内只能调用一次
        String lastRefreshTime = RedisUtils.getCacheObject("lastRefreshTime");
        if(StrUtil.isEmpty(lastRefreshTime)) {
            RedisUtils.setCacheObject("lastRefreshTime", DateUtil.now());
            bingWallpaperAutoAcquireJob.dayAcquireBingJsonJobHandler();
        } else {
            long hours = DateUtil.between(DateUtil.parse(lastRefreshTime, DatePattern.NORM_DATETIME_FORMAT), DateUtil.date(), DateUnit.HOUR);
            if (hours > 0) {
                RedisUtils.setCacheObject("lastRefreshTime", DateUtil.now());
                bingWallpaperAutoAcquireJob.dayAcquireBingJsonJobHandler();
            }
        }
        byte[] image = imageUtils.getImageUrl(null, null, null, null, null);
        imageUtils.backImage(image, null, request, response);
    }

    /**
     * 更新指定日期缓存
     *
     */
    @GetMapping("/refreshDay/{day}")
    public void refreshDayCache(@PathVariable String day, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 刷新指定日期数据
        RedisUtils.incrAtomicValue("visitCount");
        // 限制一个小时之内只能调用一次
        String lastRefreshTime = RedisUtils.getCacheObject("lastRefreshTime");
        if(StrUtil.isEmpty(lastRefreshTime)) {
            RedisUtils.setCacheObject("lastRefreshTime", DateUtil.now());
            bingWallpaperAutoAcquireJob.assignDayAcquireOriginBingJsonJobHandler(day);
        } else {
            long hours = DateUtil.between(DateUtil.parse(lastRefreshTime, DatePattern.NORM_DATETIME_FORMAT), DateUtil.date(), DateUnit.HOUR);
            if (hours > 0) {
                RedisUtils.setCacheObject("lastRefreshTime", DateUtil.now());
                bingWallpaperAutoAcquireJob.assignDayAcquireOriginBingJsonJobHandler(day);
            }
        }
        byte[] image = imageUtils.getImageUrl(null, null, null, null, null);
        imageUtils.backImage(image, null, request, response);
    }



}
