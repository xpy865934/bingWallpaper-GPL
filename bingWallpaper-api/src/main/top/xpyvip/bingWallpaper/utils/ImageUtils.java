package top.xpyvip.bingWallpaper.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import top.xpyvip.bingWallpaper.domain.BingWallpaperInfo;
import top.xpyvip.bingWallpaper.enums.ImageResolution;
import top.xpyvip.bingWallpaper.utils.redis.RedisUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ImageUtils {

    @Value("${bingWallpaper.imagePath}")
    private String imagePath;

    /**
     * 根据分辨率返回对应分辨率图片url
     * w=1920x1080 表示输出1080P分辨率图像，w=1600 表示自定义宽度为1600px的图像
     * 固定分辨率支持如下： UHD , 1920x1200 , 1920x1080 , 1366x768 , 1280x768 ,1024x768 , 800x600 , 800x480 , 768x1280 ,
     * 720x1280 , 640x480 , 480x800 , 400x240 , 320x240 , 240x320
     * 其他说明： w参数可以去掉，默认输出当天1920x1080分辨率图片
     * @param bingWallpaperInfo
     * @param size
     * @param width
     * @param height
     * @return
     */
    public byte[] getImageUrl(BingWallpaperInfo bingWallpaperInfo, String size, String width, String height)  {
        byte[] content = null;
        try {
            if (ObjUtil.isEmpty(bingWallpaperInfo)) {
                // 默认是当前最新
                // 查询redis中最新数据
                bingWallpaperInfo = RedisUtils.getCacheObject("lastInfo");
            }
            // 判断size是否为空，如果size不为空且在分辨率内，优先使用size的大小
            ImageResolution imageResolution = ImageResolution.find(size);
            if (StrUtil.isNotEmpty(size) && ObjUtil.isNotEmpty(imageResolution)) {
                // 获取文件
                ApplicationHome h = new ApplicationHome(getClass());
                File jarF = h.getSource();
                String imagePath = jarF.getParentFile().toString() + File.separator + this.imagePath;
                imagePath = imagePath + File.separator + DateUtil.format(bingWallpaperInfo.getStartTime(), DatePattern.NORM_YEAR_PATTERN)
                        + File.separator + DateUtil.format(bingWallpaperInfo.getStartTime(), "MM") + File.separator
                        + DateUtil.format(bingWallpaperInfo.getStartTime(), "dd") + "_" + size + ".jpg";
                content = IoUtil.readBytes(new FileInputStream(imagePath));
                return content;
            } else {
                size = ImageResolution.I1920X1080.getResolution();
                // 获取文件
                ApplicationHome h = new ApplicationHome(getClass());
                File jarF = h.getSource();
                String imagePath = jarF.getParentFile().toString() + File.separator + this.imagePath;
                imagePath = imagePath + File.separator + DateUtil.format(bingWallpaperInfo.getStartTime(), DatePattern.NORM_YEAR_PATTERN)
                        + File.separator + DateUtil.format(bingWallpaperInfo.getStartTime(), "MM") + File.separator
                        + DateUtil.format(bingWallpaperInfo.getStartTime(), "dd") + "_" + size + ".jpg";
                // size为空或者不在分辨率中，此时判断高度和宽度
                if (StrUtil.isNotEmpty(width) && NumberUtil.isInteger(width) && Integer.parseInt(width) <= 3840 && Integer.parseInt(width) > 0
                        && StrUtil.isNotEmpty(height) && NumberUtil.isInteger(height) && Integer.parseInt(height) <= 2160 && Integer.parseInt(height) > 0) {
                    // 直接转换高度和宽度
                    BufferedImage bufferedImage = Thumbnails.of(imagePath).size(Integer.parseInt(width), Integer.parseInt(height)).keepAspectRatio(false).asBufferedImage();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
                    return byteArrayOutputStream.toByteArray();
                } else if (StrUtil.isNotEmpty(width) && NumberUtil.isInteger(width) && Integer.parseInt(width) <= 3840 && Integer.parseInt(width) > 0) {
                    BufferedImage bufferedImage = Thumbnails.of(imagePath).width(Integer.parseInt(width)).asBufferedImage();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
                    return byteArrayOutputStream.toByteArray();
                } else if (StrUtil.isNotEmpty(height) && NumberUtil.isInteger(height) && Integer.parseInt(height) <= 2160 && Integer.parseInt(height) > 0) {
                    BufferedImage bufferedImage = Thumbnails.of(imagePath).height(Integer.parseInt(height)).asBufferedImage();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
                    return byteArrayOutputStream.toByteArray();
                }
                content = IoUtil.readBytes(new FileInputStream(imagePath));
                return content;
            }
        } catch (Exception e) {
            // 异常直接返回最新的数据
            // 查询redis中最新数据
            log.error(e.getMessage(), e);
            bingWallpaperInfo = RedisUtils.getCacheObject("lastInfo");
            if(ObjUtil.isNotEmpty(bingWallpaperInfo)) {
                return HttpUtil.downloadBytes("https://cn.bing.com" + bingWallpaperInfo.getUrlbase() + "_UHD.jpg");
            } else {
                throw new RuntimeException("系统异常");
            }
        }
    }

    public void backImage(byte[] image, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置 contentType
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        // 输出附件
        IoUtil.write(response.getOutputStream(), false, image);
    }
}
