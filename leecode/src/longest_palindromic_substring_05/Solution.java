package longest_palindromic_substring_05;



/**
 * Created by 胡歌的小迷弟 on 2020/1/17 19:54
 */
class Solution {


    public static void main(String[] args) {
        System.out.println(longestPalindrome2("abba"));
    }

    public static String longestPalindrome(String s) {
        char[] c=s.toCharArray();
        int max=0;
        String str="";
        for(int i=0;i<c.length;i++){
            for(int j=i+1;j<c.length;j++){
                if(isPalindrome(s.substring(i,j+1))){
                    if(max<j-i){
                        max=j-i;
                        str=s.substring(i,j+1);
                    }
                }
            }

        }
        return str;
    }


    public static boolean isPalindrome(String str){
        for(int i=0;i<(str.length())/2;i++){
            if(str.charAt(i)!=str.charAt(str.length()-i-1)){
                return false;
            }
        }
        return true;
    }


    /**
     * 动态规划
     * @param s
     * @return
     */

    public static String longestPalindrome2(String s) {
        int len = s.length();
        if (len < 2) {
            return s;
        }

        boolean[][] dp = new boolean[len][len];

        // 初始化
        for (int i = 0; i < len; i++) {
            dp[i][i] = true;
        }

        int maxLen = 1;
        int start = 0;

        for (int j = 1; j < len; j++) {
            for (int i = 0; i < j; i++) {

                if (s.charAt(i) == s.charAt(j)) {
                    if (j - i < 3) {
                        dp[i][j] = true;
                    } else {
                        dp[i][j] = dp[i + 1][j - 1];
                    }
                } else {
                    dp[i][j] = false;
                }

                // 只要 dp[i][j] == true 成立，就表示子串 s[i, j] 是回文，此时记录回文长度和起始位置
                if (dp[i][j]) {
                    int curLen = j - i + 1;
                    if (curLen > maxLen) {
                        maxLen = curLen;
                        start = i;
                    }
                }
            }
        }
        return s.substring(start, start + maxLen);
    }
}


