package l03_longest_substring_without_repeating_characters;

/**
 * Created by 胡歌的小迷弟 on 2019/12/23 21:41
 */



/**
 * Given a string, find the length of the longest substring without repeating characters.
 *
 * Example 1:
 *
 * Input: "abcabcbb"
 * Output: 3
 * Explanation: The answer is "abc", with the length of 3.
 * Example 2:
 *
 * Input: "bbbbb"
 * Output: 1
 * Explanation: The answer is "b", with the length of 1.
 * Example 3:
 *
 * Input: "pwwkew"
 * Output: 3
 * Explanation: The answer is "wke", with the length of 3.
 *              Note that the answer must be a substring, "pwke" is a subsequence and not a substring.
 */
public class Solution {


    public static void main(String[] args) {
        System.out.println(lengthOfLongestSubstring("abcabcbb"));
    }
    public static  int lengthOfLongestSubstring(String s) {

        int maxLength = 0;
        char[] chars = s.toCharArray();
        int repeatIndex = 0;//重复的字符的下标
        for(int i=0;i<chars.length;i++){
            for(int j=repeatIndex;j<i;j++){
                if(chars[i]==chars[j]){
                    maxLength=Math.max(maxLength,i-repeatIndex);
                    repeatIndex=j+1;
                    break;
                }
            }
        }
        return Math.max(chars.length - repeatIndex, maxLength);
    }


}
