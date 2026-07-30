package l09_palindrome_number;

import java.math.BigDecimal;

/**
 * Created by 胡歌的小迷弟 on 2019/12/23 21:41
 */



class Solution {

    public boolean isPalindrome(int x) {
        String s=x+"";
        for(int i=0;i<=s.length()/2;i++){
            if(s.charAt(i)!=s.charAt(s.length()-i-1)){
                return false;
            }
        }
        return true;
    }
}