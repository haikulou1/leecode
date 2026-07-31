package cn.wy.helloworld;

/**
 * HelloWorld 接口。
 * <p>提供无参问候 {@link #sayHello()} 与指定名称问候 {@link #sayHello(String)} 两种形态。
 *
 * @author wy
 */
public interface HelloWorld {

    /**
     * 返回固定问候语 "Hello, World!"。
     *
     * @return "Hello, World!"
     */
    String sayHello();

    /**
     * 返回带指定名称的问候语 "Hello, &lt;name&gt;!"。
     *
     * @param name 被问候对象名称，为 null 或空串时退化为 "World"
     * @return 形如 "Hello, Java!" 的问候语
     */
    String sayHello(String name);
}
