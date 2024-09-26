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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * 返回当天1080p的图像
     *
     */
    @GetMapping()
    public void get(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RedisUtils.incrAtomicValue("visitCount");
        LambdaQueryWrapper<BingWallpaperInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(BingWallpaperInfo::getStartTime,DateUtil.format(DateUtil.date(), DatePattern.NORM_DATE_FORMAT));
        BingWallpaperInfo bingWallpaperInfo = iBingWallpaperInfoService.getOne(queryWrapper);
        if(ObjUtil.isEmpty(bingWallpaperInfo)){
            // 查询数据库中当前最大值
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(BingWallpaperInfo::getStartTime).last("limit 1");
            bingWallpaperInfo = iBingWallpaperInfoService.getOne(queryWrapper);
        }
        byte[] image = getImageUrl(bingWallpaperInfo, null);
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
        LambdaQueryWrapper<BingWallpaperInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(BingWallpaperInfo::getStartTime,DateUtil.format(dateTime, DatePattern.NORM_DATE_FORMAT));
        BingWallpaperInfo bingWallpaperInfo = iBingWallpaperInfoService.getOne(queryWrapper);
        if(ObjUtil.isEmpty(bingWallpaperInfo)){
            // 查询数据库中当前最大值
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(BingWallpaperInfo::getStartTime).last("limit 1");
            bingWallpaperInfo = iBingWallpaperInfoService.getOne(queryWrapper);
        }
        byte[] image = getImageUrl(bingWallpaperInfo, w);
        backImage(image, request, response);
    }

    private void backImage(byte[] image, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if(ObjUtil.isEmpty(image)){
            // 返回当天1080p图像
            LambdaQueryWrapper<BingWallpaperInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.likeRight(BingWallpaperInfo::getStartTime,DateUtil.format(DateUtil.date(), DatePattern.NORM_DATE_FORMAT));
            BingWallpaperInfo bingWallpaperInfo = iBingWallpaperInfoService.getOne(queryWrapper);
            if(ObjUtil.isEmpty(bingWallpaperInfo)){
                // 查询数据库中当前最大值
                queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.orderByDesc(BingWallpaperInfo::getStartTime).last("limit 1");
                bingWallpaperInfo = iBingWallpaperInfoService.getOne(queryWrapper);
            }
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
