package l10_regular_expression_matching;

/**
 * Created by 胡歌的小迷弟 on 2019/12/23 21:41
 */



class Solution {


    public static void main(String[] args) {
        System.out.println(isMatch("aab","c*a*b"));
    }


    //'.' Matches any single character.
     //'*' Matches zero or more of the preceding element.
    public static boolean isMatch(String text, String pattern) {
            boolean[][] dp = new boolean[text.length() + 1][pattern.length() + 1];
            dp[text.length()][pattern.length()] = true;
            for (int i = text.length(); i >= 0; i--){
                for (int j = pattern.length() - 1; j >= 0; j--){
                    boolean first_match = (i < text.length() &&
                            (pattern.charAt(j) == text.charAt(i) ||
                                    pattern.charAt(j) == '.'));
                    if (j + 1 < pattern.length() && pattern.charAt(j+1) == '*'){
                        dp[i][j] = dp[i][j+2] || first_match && dp[i+1][j];
                    } else {
                        dp[i][j] = first_match && dp[i+1][j+1];
                    }
                }
            }
            return dp[0][0];
        }


}