package 栈;

import javax.xml.bind.SchemaOutputResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * Created by 胡歌的小迷弟 on 2019/9/19 19:33
 */
public class 中缀表达式转后缀表达式 {


    private static int ADD = 1;
    private static int SUB = 1;
    private static int MUL = 2;
    private static int DIV = 2;

    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        while (scanner.hasNext()) {
            String str = scanner.nextLine();
            List<String >list=min2end(strtolist(str));
            for(String s:list){
                System.out.print(s+" ");
            }
        }
    }

        //1+((2+3)*4)-5   123+4*5-
    public static List<String> min2end(List<String> ls){
        Stack<String> s1=new Stack<>();
        List<String>s2=new ArrayList<>();

        for(String s:ls){
            if(s.matches("\\d+")) {
                s2.add(s);
            }else if(s.equals("(")){
                s1.push(s);
            }else if(s.equals(")")){
                //如果是右括号“)”，则依次弹出s1栈顶的运算符，并压入s2，直到遇到左括号为止，此时将这一对括号丢弃
                while (!s1.peek().equals("(")){
                    s2.add(s1.pop());
                }
                s1.pop();//!!! 将 ( 弹出 s1栈， 消除小括号
            }else {
                //当s的优先级小于等于s1栈顶运算符, 将s1栈顶的运算符弹出并加入到s2中，再次转到(4.1)与s1中新的栈顶运算符相比较
                while(s1.size()!=0&&getValue(s)<=getValue(s1.peek())){
                    s2.add(s1.pop());
                }
                s1.push(s);
            }
        }

        //将s1中剩余的运算符依次弹出并加入s2
        while(s1.size() != 0) {
            s2.add(s1.pop());
        }
        return s2;
    }


    /**
     * 把数字和符号拆分出来
     * @param s
     * @return
     */
    public  static List<String> strtolist(String s){
        List<String> ls=new ArrayList<>();
        int i=0;
        String str;
        char c;
        do {
            if((c=s.charAt(i))<48||(c=s.charAt(i))>57){
                ls.add(c+"");
                i++;
            }else {
                str="";
                while (i < s.length() &&(c=s.charAt(i))>=48&&(c=s.charAt(i))<=57){
                    str+=c;
                    i++;
                }
                ls.add(str);
            }
        }while (i<s.length());
        return ls;
    }



    //写一个方法，返回对应的优先级数字
    public static int getValue(String operation) {
        int result = 0;
        switch (operation) {
            case "+":
                result = ADD;
                break;
            case "-":
                result = SUB;
                break;
            case "*":
                result = MUL;
                break;
            case "/":
                result = DIV;
                break;

        }
        return result;
    }
}
