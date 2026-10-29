public class HelloWorld {
    public static void main(String[] args) {
        try {
            System.out.println("Hello, World!");
            System.out.flush();
        } catch (RuntimeException e) {
            System.err.println("HelloWorld 异常兜底：标准输出不可用。原因: " + e.getMessage());
            System.err.flush();
            System.exit(1);
        }
    }
}
