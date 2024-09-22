package top.xpyvip.bingWallpaper.services.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.xpyvip.bingWallpaper.domain.BingWallpaperInfo;
import top.xpyvip.bingWallpaper.mapper.BingWallpaperInfoMapper;
import top.xpyvip.bingWallpaper.services.IBingWallpaperInfoService;

/**
 * Bing壁纸信息
 */
@RequiredArgsConstructor
@Service
public class BingWallpaperInfoServiceImpl extends ServiceImpl<BingWallpaperInfoMapper, BingWallpaperInfo> implements IBingWallpaperInfoService {
}
