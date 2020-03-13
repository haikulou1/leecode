package cn.wy.mybatis1;


import cn.wy.beans.Test;

public interface TestMapper {
    Test selectByPrimaryKey(Integer userId);
}