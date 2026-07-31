package l08_string_2_integer;

import java.math.BigDecimal;

/**
 * Created by 胡歌的小迷弟 on 2019/12/23 21:41
 */



class Solution {


    public static void main(String[] args) {
      int i=myAtoi("- 234");
        System.out.println(i);
    }

    public static int myAtoi(String str) {

        char[] c=str.toCharArray();

        if(str.trim().length()==0){
            return 0;
        }

        if(str.trim().length()==1){
            if(str.charAt(0)>='0'&&str.charAt(0)<='9'){
                return Integer.parseInt(str);
            }else{
                return 0;
            }
        }

        boolean first=false;
        String s="";
        for(int i=0;i<c.length;i++) {
            if(c[i]>='0'&&c[i]<='9'){
                s+=c[i];
                first=true;
            }else {
                if (first) {
                    break;
                }
            }
        }

if(s.length()<=0){
    return 0;
}

        int i=1;
        int start=str.indexOf(s.charAt(0) + "");

        boolean flag=true;
        for(int j=start-1;j>=0;j--){
            if((c[j]=='-'||c[j]=='+')&&flag){
                if(j!=start-1){
                    return 0;
                }
                flag=false;
                continue;
            }
            else if(c[j]!=' '){
                return 0;
            }

        }

        if(start-1>=0) {
            char p=str.charAt(start-1);
            if ( p== '-') {
                i = -1;
            }
        }
        BigDecimal bigDecimal=new BigDecimal(s);
        if(i*bigDecimal.doubleValue()>=Integer.MAX_VALUE){
            return Integer.MAX_VALUE;
        }else if(i*bigDecimal.doubleValue()<=Integer.MIN_VALUE){
            return Integer.MIN_VALUE;
        }


        return i*Integer.parseInt(s);
    }
}