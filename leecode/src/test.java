import javax.xml.bind.SchemaOutputResolver;

/**
 * Created by 胡歌的小迷弟 on 2019/12/24 21:42
 */
public class test {
    public static void main(String[] args) {
       String s=new String("1");
       s.intern();
       String s2="1";
        System.out.println(s==s2);


        String s1=new String("1")+new String("1");
        s1.intern();
        String s4="11";

        System.out.println(s1==s4);
    }
}
