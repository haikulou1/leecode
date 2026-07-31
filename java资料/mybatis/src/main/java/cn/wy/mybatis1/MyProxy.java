package cn.wy.mybatis1;

import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by 胡歌的小迷弟 on 2020/3/13 15:23
 */
public class MyProxy implements InvocationHandler {

    private MySqlSession mySqlSession;

    private Class interfaces;


    public MyProxy(MySqlSession mySqlSession, Class clazz) {
        this.mySqlSession=mySqlSession;
        this.interfaces=clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


        return mySqlSession.selectOne();
    }
}
