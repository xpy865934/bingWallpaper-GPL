package top.xpyvip.bingWallpaper.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.xpyvip.bingWallpaper.config.properties.SiteProperties;
import top.xpyvip.bingWallpaper.domain.BingWallpaperInfo;
import top.xpyvip.bingWallpaper.domain.PageQuery;
import top.xpyvip.bingWallpaper.domain.TableDataInfo;
import top.xpyvip.bingWallpaper.enums.ImageResolution;
import top.xpyvip.bingWallpaper.services.IBingWallpaperInfoService;
import top.xpyvip.bingWallpaper.utils.ImageUtils;
import top.xpyvip.bingWallpaper.utils.redis.RedisUtils;

import javax.annotation.Resource;

@Slf4j
@Validated
@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class BingWallpaperController {

    @Resource
    private IBingWallpaperInfoService iBingWallpaperInfoService;

    @Resource
    private SiteProperties siteProperties;

    @Resource
    private ImageUtils imageUtils;

    /**
     *  index
     */
    @GetMapping({"/", "/index", "/index.html", "index.htm"})
    public String get(@RequestParam(defaultValue = PageQuery.DEFAULT_PAGE_NUM+"") int pageNum, @RequestParam(defaultValue = "21") int pageSize, Model model) throws Exception {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(pageNum);
        pageQuery.setPageSize(pageSize);
        TableDataInfo<BingWallpaperInfo> tableDataInfo = iBingWallpaperInfoService.getBingWallpaperInfoPage(pageQuery);
        model.addAttribute("tableData", tableDataInfo);

        BingWallpaperInfo bingWallpaperInfo = RedisUtils.getCacheObject("lastInfo");
        byte[] imageUrl = imageUtils.getImageUrl(bingWallpaperInfo, ImageResolution.I400X240.getResolution());
        String jpg = ImgUtil.toBase64(ImgUtil.toImage(imageUrl), "jpg");
        model.addAttribute("backGroundImg", "data:image/jpg;base64," + jpg);
        this.fillCommonModel(model);
        return "index";
    }

    /**
     *  detail
     */
    @GetMapping({"/detail","/detail/{day}", "/detail.html/{day}", "detail.htm/{day}"})
    public String get(@PathVariable(required = false) String day, Model model) throws Exception {
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
        if(ObjUtil.isEmpty(bingWallpaperInfo)) {
            // 查询redis中最新数据
            bingWallpaperInfo = RedisUtils.getCacheObject("lastInfo");
            day = DateUtil.format(bingWallpaperInfo.getStartTime(), DatePattern.PURE_DATE_PATTERN);
        }
        // 图片不使用api接口，直接转换成base64
        byte[] imageUrl = imageUtils.getImageUrl(bingWallpaperInfo, ImageResolution.I1920X1080.getResolution());
        String jpg = ImgUtil.toBase64(ImgUtil.toImage(imageUrl), "jpg");
        model.addAttribute("detailUrl", "data:image/jpg;base64," + jpg);
        model.addAttribute("desc", bingWallpaperInfo.getTitle() + " | " + bingWallpaperInfo.getCopyright() + " | " + DateUtil.format(bingWallpaperInfo.getStartTime(), DatePattern.PURE_DATE_PATTERN));
        model.addAttribute("story", bingWallpaperInfo.getStory());
        // 如果日期小于2010-01-01
        if (DateUtil.parse("2010-01-02", DatePattern.NORM_DATE_FORMAT).after(bingWallpaperInfo.getStartTime())) {
            model.addAttribute("lastPage", null);
        } else {
            model.addAttribute("lastPage", "./" + DateUtil.format(DateUtil.offsetDay(bingWallpaperInfo.getStartTime(), -1), DatePattern.PURE_DATE_PATTERN));
        }
        // 如果日期大于当前日期
        if (DateUtil.offsetDay(DateUtil.date(), -1).before(bingWallpaperInfo.getStartTime())) {
            model.addAttribute("nextPage", null);
        } else {
            model.addAttribute("nextPage", "./" + DateUtil.format(DateUtil.offsetDay(bingWallpaperInfo.getStartTime(), 1), DatePattern.PURE_DATE_PATTERN));
        }
        this.fillCommonModel(model);
        return "detail";
    }

    private void fillCommonModel(Model model) {
        // 通用设置
        model.addAttribute("title", siteProperties.getTitle());
        model.addAttribute("name", siteProperties.getName());
        model.addAttribute("url", siteProperties.getUrl());
        model.addAttribute("author", siteProperties.getAuthor());
        model.addAttribute("keywords", siteProperties.getKeywords());
        model.addAttribute("description", siteProperties.getDescription());
        model.addAttribute("shortDesc", siteProperties.getShortDesc());
        model.addAttribute("feedBackUrl", siteProperties.getFeedBackUrl());
    }
}
