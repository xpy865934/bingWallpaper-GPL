package top.xpyvip.bingWallpaper.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bing_wallpaper_info")
public class BingWallpaperInfo extends BaseEntity{

    /**
     * id
     */
    @TableId
    private Integer id;

    /**
     * 日期
     */
    private Date startTime;

    /**
     * 原始访问链接
     */
    private String url;

    /**
     * 基础访问链接
     */
    private String urlbase;

    /**
     * 标题
     */
    private String title;

    /**
     * 版权
     */
    private String copyright;

    /**
     * 版权链接
     */
    private String copyrightlink;

    /**
     * hsh
     */
    private String hsh;

    /**
     * 故事
     */
    private String story;


    private String startdate;


    private String fullstartdate;


    private String enddate;


    private String quiz;

    /**
     * 浏览数量
     */
    private int views;

    /**
     * 收藏数量
     */
    private int favorites;
}
