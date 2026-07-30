package cn.wy;

import cn.wy.dubbo.Service;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.Protocol;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        ClassPathXmlApplicationContext classPathXmlApplicationContext
                =new ClassPathXmlApplicationContext("dubbo-client.xml");
        Service service= (Service) classPathXmlApplicationContext.getBean("myservice");
        System.out.println(service.say(""));


        Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class)
                .getExtension("myProtocol");

        System.out.println(protocol.getDefaultPort());
    }
}
