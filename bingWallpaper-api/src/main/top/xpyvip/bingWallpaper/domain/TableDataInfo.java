package top.xpyvip.bingWallpaper.domain;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 *
 * @author Lion Li
 */

@Data
@NoArgsConstructor
public class TableDataInfo<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 列表数据
     */
    private List<T> rows;

    /**
     * 当前页码
     */
    private long current;

    /**
     * 当前分页条数
     */
    private long size;

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<T> list, int total, long current, long size) {
        this.rows = list;
        this.total = total;
        this.current = current;
        this.size = size;
    }

    public static <T> TableDataInfo<T> build(IPage<T> page) {
        TableDataInfo<T> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());
        rspData.setCurrent(page.getCurrent());
        rspData.setSize(page.getSize());
        return rspData;
    }

    public static <T> TableDataInfo<T> build(List<T> list, long current, long size) {
        TableDataInfo<T> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(list.size());
        rspData.setCurrent(current);
        rspData.setSize(size);
        return rspData;
    }

    public static <T> TableDataInfo<T> build() {
        TableDataInfo<T> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        return rspData;
    }

    public long getPages(){
        if(total % size == 0){
            return total / size;
        }else {
            return total / size + 1;
        }
    }

    /**
     * 获取起始页码
     */
    public long getFrom(){
        long from = current -2;
        return Math.max(from, 1);
    }
    /**
     * 获取结束页码
     */
    public long getTo(){
        long to = current + 2;
        long pages = getPages();
        return Math.min(to, pages);
    }
}
