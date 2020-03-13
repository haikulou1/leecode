package cn.wy.mybatis1;

import java.lang.reflect.Proxy;

/**
 * Created by 胡歌的小迷弟 on 2020/3/13 15:15
 */
public class MySqlSession {



    private MyExecutor myExecutor;

    public MySqlSession(MyExecutor myExecutor) {
        this.myExecutor = myExecutor;
    }

    public <T>  T getMapper(Class clazz){
        return (T)Proxy.newProxyInstance(MySqlSession.class.getClassLoader(),
                new Class[]{clazz},new MyProxy(this,clazz));
    }



    public <T> T selectOne(){

        return myExecutor.selectOne(TestMapperXml.methodSqlMapping.get("selectByPrimaryKey"),null);
    }

}
