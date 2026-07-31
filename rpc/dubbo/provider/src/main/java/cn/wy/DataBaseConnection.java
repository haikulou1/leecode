package cn.wy;

import cn.wy.spi.DataBaseDirver;

import java.util.ServiceLoader;

/**
 * Created by 胡歌的小迷弟 on 2020/3/8 14:40
 */
public class DataBaseConnection {

    public static void main(String[] args) {
        ServiceLoader<DataBaseDirver> serviceLoader=
                ServiceLoader.load(DataBaseDirver.class);

        for(DataBaseDirver dataBaseDirver:serviceLoader){
            System.out.println(dataBaseDirver.connect("localhost"));
        }
    }
}
