package cn.wy.helloworld;

/**
 * {@link HelloWorld} 的默认实现。
 *
 * @author wy
 */
public class HelloWorldImpl implements HelloWorld {

    /** {@inheritDoc} */
    public String sayHello() {
        return "Hello, World!";
    }

    /** {@inheritDoc} */
    public String sayHello(String name) {
        if (name == null || name.length() == 0) {
            return sayHello();
        }
        return "Hello, " + name + "!";
    }
}
