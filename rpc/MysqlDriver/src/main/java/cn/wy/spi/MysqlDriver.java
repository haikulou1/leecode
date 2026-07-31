package cn.wy.spi;

/**
 * Created by 胡歌的小迷弟 on 2020/3/8 14:32
 */
public class MysqlDriver implements DataBaseDirver {
    @Override
    public String connect(String host) {
        return "mysql connect";
    }
}
