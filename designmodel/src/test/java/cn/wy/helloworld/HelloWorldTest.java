package cn.wy.helloworld;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link HelloWorldImpl} 单元测试。
 *
 * @author wy
 */
public class HelloWorldTest {

    private final HelloWorld hello = new HelloWorldImpl();

    @Test
    public void sayHello_returnsHelloWorld() {
        Assert.assertEquals("Hello, World!", hello.sayHello());
    }

    @Test
    public void sayHelloWithName_returnsHelloName() {
        Assert.assertEquals("Hello, Java!", hello.sayHello("Java"));
    }

    @Test
    public void sayHelloWithEmptyName_fallsBackToWorld() {
        Assert.assertEquals("Hello, World!", hello.sayHello(""));
        Assert.assertEquals("Hello, World!", hello.sayHello(null));
    }
}
