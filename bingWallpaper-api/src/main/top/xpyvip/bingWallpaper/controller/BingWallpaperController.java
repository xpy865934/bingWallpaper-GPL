package top.xpyvip.bingWallpaper.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xpyvip.bingWallpaper.jobs.BingWallpaperAutoAcquireJob;
import top.xpyvip.bingWallpaper.services.IBingWallpaperInfoService;

import javax.annotation.Resource;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/")
public class BingWallpaperController {

    @Resource
    private IBingWallpaperInfoService iBingWallpaperInfoService;

    @Resource
    private BingWallpaperAutoAcquireJob bingWallpaperAutoAcquireJob;

    /**
     * 返回当天1080p的图像
     *
     */
    @GetMapping("/day")
    public Boolean get() throws Exception {
        return true;
    }
}
