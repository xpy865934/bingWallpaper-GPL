package top.xpyvip.bingWallpaper.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import top.xpyvip.bingWallpaper.domain.BingWallpaperInfo;
import top.xpyvip.bingWallpaper.jobs.BingWallpaperAutoAcquireJob;
import top.xpyvip.bingWallpaper.services.IBingWallpaperInfoService;
import top.xpyvip.bingWallpaper.utils.StringUtils;
import top.xpyvip.bingWallpaper.utils.redis.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

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

    private static final List<String> IMAGE_RESOLUTION = Arrays.asList(new String[]{"UHD", "1920x1200", "1920x1080", "1366x768", "1280x768",
            "1024x768", "800x600" , "800x480" , "768x1280", "720x1280" , "640x480" , "480x800" , "400x240" , "320x240" , "240x320"});

    @Value("${bingWallpaper.imagePath}")
    private String imagePath;

    /**
     * 返回指定日期的图像， 默认如果day是空的，返回当天的图像，如果都不传，默认是当天1080p的图像
     *
     */
    @GetMapping()
    public void get(String day, String w, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
        byte[] image = getImageUrl(bingWallpaperInfo, w);
        backImage(image, request, response);
    }

    /**
     * 返回当天4K图像
     *
     */
    @GetMapping("/4K")
    public void get4K(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RedisUtils.incrAtomicValue("visitCount");
        DateTime now = DateUtil.date();
        // 此处从redis查询，不直接从数据库查询
        BingWallpaperInfo bingWallpaperInfo = RedisUtils.getCacheObject(DateUtil.format(now, DatePattern.PURE_DATE_PATTERN));
        byte[] image = getImageUrl(bingWallpaperInfo, "UHD");
        backImage(image, request, response);
    }

    /**
     * 返回随机图像
     *
     */
    @GetMapping("/random")
    public void random(String w, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
        byte[] image = getImageUrl(bingWallpaperInfo, w);
        backImage(image, request, response);
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
        byte[] image = getImageUrl(null, null);
        backImage(image, request, response);
    }

    private void backImage(byte[] image, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if(ObjUtil.isEmpty(image)){
            // 查询redis中最新数据
            BingWallpaperInfo bingWallpaperInfo = RedisUtils.getCacheObject("LastInfo");
            image = getImageUrl(bingWallpaperInfo, null);
        }
        // 设置 contentType
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        // 输出附件
        IoUtil.write(response.getOutputStream(), false, image);
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
    private byte[] getImageUrl(BingWallpaperInfo bingWallpaperInfo, String w) {
        byte[] content = null;
        if(ObjUtil.isEmpty(bingWallpaperInfo)){
            return content;
        }
        if(StrUtil.isEmpty(w)) {
            w = "1920x1080";
        }
        // 不包含，判断日期是否小于2022-04-27
        if(DateUtil.parse("2022-04-27", DatePattern.NORM_DATE_FORMAT).after(bingWallpaperInfo.getStartTime())) {
            String resolution = "1920x1080";
            if(IMAGE_RESOLUTION.contains(w)) {
                resolution = w;
            }
            // 获取文件
            ApplicationHome h = new ApplicationHome(getClass());
            File jarF = h.getSource();
            System.out.println(jarF.getParentFile().toString());
            String imagePath = jarF.getParentFile().toString() + File.separator + this.imagePath;
            imagePath = imagePath + File.separator + DateUtil.format(bingWallpaperInfo.getStartTime(), DatePattern.NORM_YEAR_PATTERN)
                    + File.separator + DateUtil.format(bingWallpaperInfo.getStartTime(), "MM") + File.separator
                    + DateUtil.format(bingWallpaperInfo.getStartTime(), "dd") +  "_" + resolution + ".jpg";

            try {
                content = IoUtil.readBytes(new FileInputStream(imagePath));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return content;
        } else {
            // 判断图像分辨率是否包含
            if(!IMAGE_RESOLUTION.contains(w)) {
                // 获取w字符串中最大的数值
                w = String.valueOf(StringUtils.getMaxNum(w));
                return HttpUtil.downloadBytes("https://cn.bing.com" + bingWallpaperInfo.getUrlbase() + "_UHD.jpg&w=" + w);
            }
            return HttpUtil.downloadBytes("https://cn.bing.com" + bingWallpaperInfo.getUrlbase() + "_" + w + ".jpg");
        }
    }
}
