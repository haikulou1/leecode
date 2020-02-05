package l10_regular_expression_matching;

/**
 * Created by 胡歌的小迷弟 on 2019/12/23 21:41
 */



class Solution {


    //'.' Matches any single character.
     //'*' Matches zero or more of the preceding element.
    public boolean isMatch(String s, String p) {
        if(s==null||"".equals(s)||p==null||"".equals(p)){
            return false;
        }
        return true;
    }
}