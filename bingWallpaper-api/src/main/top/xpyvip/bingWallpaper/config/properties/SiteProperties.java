package top.xpyvip.bingWallpaper.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 网站配置类
 *
 * @author Lion Li
 */
@Data
@ConfigurationProperties(prefix = "bing-wallpaper.site")
@Component
public class SiteProperties {

    private String title;

    private String name;

    private String url;

    private String author;

    private String keywords;

    private String description;

    private String shortDesc;

    private String feedBackUrl;
}
