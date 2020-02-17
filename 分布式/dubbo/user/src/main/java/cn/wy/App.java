package cn.wy;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("order-consumer.xml");

        //用户下单过程
        Service services=(Service)context.getBean("orderServices");

        DoOrderRequest request=new DoOrderRequest();
        request.setName("mic");
        DoOrderResponse response=services.say(request);

        System.out.println(response);

        //Order.doOrder();
        System.in.read();
    }
}
