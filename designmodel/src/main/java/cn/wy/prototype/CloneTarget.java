package cn.wy.prototype;

import java.io.Serializable;

/**
 * Created by 胡歌的小迷弟 on 2020/3/10 11:02
 */
public class CloneTarget implements Cloneable, Serializable {

    private static final long serialVersionUID = 325845643401053912L;
    public String name;

    public Person person;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
