package top.xpyvip.bingWallpaper.services;

import com.baomidou.mybatisplus.extension.service.IService;
import top.xpyvip.bingWallpaper.domain.BingWallpaperInfo;
import top.xpyvip.bingWallpaper.domain.PageQuery;
import top.xpyvip.bingWallpaper.domain.TableDataInfo;

/**
 * bing壁纸信息
 */
public interface IBingWallpaperInfoService extends IService<BingWallpaperInfo> {
    TableDataInfo<BingWallpaperInfo> getBingWallpaperInfoPage(PageQuery pageQuery);
}
