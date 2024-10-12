package top.xpyvip.bingWallpaper.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.xpyvip.bingWallpaper.domain.BingWallpaperInfo;
import top.xpyvip.bingWallpaper.domain.PageQuery;
import top.xpyvip.bingWallpaper.domain.TableDataInfo;
import top.xpyvip.bingWallpaper.mapper.BingWallpaperInfoMapper;
import top.xpyvip.bingWallpaper.services.IBingWallpaperInfoService;

/**
 * Bing壁纸信息
 */
@RequiredArgsConstructor
@Service
public class BingWallpaperInfoServiceImpl extends ServiceImpl<BingWallpaperInfoMapper, BingWallpaperInfo> implements IBingWallpaperInfoService {
    @Override
    public TableDataInfo<BingWallpaperInfo> getBingWallpaperInfoPage(PageQuery pageQuery) {
        LambdaQueryWrapper<BingWallpaperInfo> lqw = new LambdaQueryWrapper<BingWallpaperInfo>();
        lqw.orderByDesc(BingWallpaperInfo::getStartTime);
        Page<BingWallpaperInfo> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }
}
