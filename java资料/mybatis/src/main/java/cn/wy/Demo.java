package cn.wy;

import cn.wy.beans.Test;
import cn.wy.mybatis1.MyExecutor;
import cn.wy.mybatis1.MySqlSession;
import cn.wy.mybatis1.TestMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class Demo {
    public static SqlSession getSqlSession() throws FileNotFoundException {
        //配置文件
        InputStream configFile = new FileInputStream(
                "C:\\wy\\code\\algorithm\\java资料\\mybatis\\src\\main\\java\\cn\\wy\\mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configFile);
        //加载配置文件得到SqlSessionFactory
        return sqlSessionFactory.openSession();
    }

    public static void main(String[] args) throws FileNotFoundException {
      //  TestMapper testMapper = getSqlSession().getMapper(TestMapper.class);
       // Test test = testMapper.selectByPrimaryKey(1);

        TestMapper testMapper=new MySqlSession(new MyExecutor()).getMapper(TestMapper.class);
        System.out.println( testMapper.selectByPrimaryKey(1));

    }
}
