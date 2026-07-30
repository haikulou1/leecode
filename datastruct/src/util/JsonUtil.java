package util;

/**
 * 零依赖 JSON 工具类：提供 JSON 字符串转义能力。
 */
public final class JsonUtil {

    private JsonUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 转义 JSON 字符串中的特殊字符。
     * 处理：引号、反斜杠、换行、回车、制表符、退格、换页等。
     */
    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * 拼接 JSON 字符串键值对：以 "key":"escapedValue" 形式返回（不含大括号）。
     */
    public static String quoteField(String key, String value) {
        return "\"" + escape(key) + "\":\"" + escape(value) + "\"";
    }

    /**
     * 拼接 JSON 字符串键值对：值为原生类型（无引号转义）。
     */
    public static String rawField(String key, String rawValue) {
        return "\"" + escape(key) + "\":" + rawValue;
    }
}
