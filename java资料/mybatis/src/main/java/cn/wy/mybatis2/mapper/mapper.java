package cn.wy.mybatis2.mapper;

import cn.wy.beans.Test;
import cn.wy.mybatis2.anotation.Select;

/**
 * Created by 胡歌的小迷弟 on 2020/3/13 16:55
 */
public interface mapper {



    @Select("select * from test where id=1")
    public Test selectById(int id);
}
