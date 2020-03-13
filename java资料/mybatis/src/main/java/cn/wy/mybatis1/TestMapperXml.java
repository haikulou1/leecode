package cn.wy.mybatis1;

import java.util.HashMap;
import java.util.Map;

public class TestMapperXml {
    public static final String nameSpace = "cn.wy.mybatis1.TestMapper";

    public static final Map<String, String> methodSqlMapping = new HashMap<String, String>();

    static {
        methodSqlMapping.put("selectByPrimaryKey", "select * from test where id =1");
    }
}