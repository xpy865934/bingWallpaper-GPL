package top.xpyvip.bingWallpaper.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_info")
public class SystemInfo extends BaseEntity{

    /**
     * id
     */
    @TableId
    private Integer id;

    /**
     * 访问量
     */
    private Long visitCount;
}
