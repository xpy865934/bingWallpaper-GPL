package top.xpyvip.bingWallpaper.jobs;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.xpyvip.bingWallpaper.domain.BingWallpaperInfo;
import top.xpyvip.bingWallpaper.services.IBingWallpaperInfoService;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class BingWallpaperAutoAcquireJob {

    private static final String BING_JSON_URL = "";
    private static final String PEAPIX_BASE_URL = "";
    private static final String PEAPIX_IMAGE_URL = PEAPIX_BASE_URL +"/bing/cn/";

    @Resource
    private IBingWallpaperInfoService bingWallpaperInfoService;

    @Value("${bingWallpaper.imagePath}")
    private String imagePath;

    /**
     * 自动获取每日壁纸信息，存入数据库中
     */
    @XxlJob("dayAcquireBingJson")
    public void dayAcquireBingJsonJobHandler() throws Exception {
        XxlJobHelper.log("自动获取每日壁纸JSON信息");
        Date oldDate = DateUtil.date();
        // 获取数据库中当前最大的日期
        LambdaQueryWrapper<BingWallpaperInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BingWallpaperInfo::getStartTime).orderByDesc(BingWallpaperInfo::getStartTime).last("limit 1");
        BingWallpaperInfo bingWallpaperInfo = bingWallpaperInfoService.getOne(queryWrapper);
        if (ObjUtil.isNotEmpty(bingWallpaperInfo)) {
            oldDate = bingWallpaperInfo.getStartTime();
        }
        //判断当前时间和旧日期之间的时间差
        List<String> dateList = CollUtil.newArrayList();
        long between = DateUtil.between(oldDate, DateUtil.date(), DateUnit.DAY);
        for (int i = 0; i < between; i++) {
            DateTime dateTime = DateUtil.offsetDay(oldDate, i+1);
            dateList.add(DateUtil.format(dateTime, DatePattern.PURE_DATE_PATTERN));
        }
        if(CollUtil.isEmpty(dateList)){
            return;
        }
        this.getBingWallpaperInfo(dateList);
    }

    /**
     * 获取所有壁纸
     */
    @XxlJob("acquireAllBingJson")
    public void acquireAllBingJsonJobHandler() throws Exception {
        XxlJobHelper.log("获取所有Bing壁纸JSON信息");
        this.getBingWallpaperInfo(null);
    }

    /**
     * 将redis访问量写入数据库
     */
    @XxlJob("saveSystemInfo")
    public void saveSystemInfo() throws Exception {
        XxlJobHelper.log("将redis访问量写入数据库");
        SystemInfo systemInfo = iSystemInfoService.getOne(new LambdaQueryWrapper<>());
        if(ObjUtil.isEmpty(systemInfo)){
            systemInfo = new SystemInfo();
        }
        systemInfo.setVisitCount(RedisUtils.getAtomicValue("visitCount"));
        if(ObjUtil.isEmpty(systemInfo.getId())){
            iSystemInfoService.save(systemInfo);
        } else {
            iSystemInfoService.updateById(systemInfo);
        }
    }

    private void getBingWallpaperInfo(List<String> dateList) {
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        System.out.println(jarF.getParentFile().toString());
        String imagePath = jarF.getParentFile().toString() + File.separator + this.imagePath;
    }
}
