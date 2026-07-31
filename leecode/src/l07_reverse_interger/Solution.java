package l07_reverse_interger;

import java.math.BigDecimal;

/**
 * Created by 胡歌的小迷弟 on 2019/12/23 21:41
 */



class Solution {


    public static void main(String[] args) {
      int i=reverse(234);
        System.out.println(i);
    }

    public static int reverse(int x) {
        int flag=x>0?1:-1;
        x=x>0?x:-x;
        int result=0;
        while(x>0){
            if(result!=(result*10)/10)return 0;
            result=result*10+x%10;
            x=x/10;
        }
        return result*flag;
    }
}