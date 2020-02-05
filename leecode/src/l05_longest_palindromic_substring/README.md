## 题目
Given a string s, find the longest palindromic substring in s. You may assume that the maximum length of s is 1000.
```java
Example 1:

Input: "babad"
Output: "bab"
Note: "aba" is also a valid answer.

Example 2:

Input: "cbbd"
Output: "bb"
```
## 解题思路

第一种方法：暴力解



第二种方法：动态规划
第 1 步：定义状态
dp[i][j] 表示子串 s[i, j] 是否为回文子串。

第 2 步：思考状态转移方程
这一步在做分类讨论（根据头尾字符是否相等），根据上面的分析得到：

dp[i][j] = (s[i] == s[j]) and dp[i + 1][j - 1]
分析这个状态转移方程：

（1）“动态规划”事实上是在填一张二维表格，i 和 j 的关系是 i <= j ，因此，只需要填这张表的上半部分；

（2）看到 dp[i + 1][j - 1] 就得考虑边界情况。

边界条件是：表达式 [i + 1, j - 1] 不构成区间，即长度严格小于 2，即 j - 1 - (i + 1) + 1 < 2 ，整理得 j - i < 3。

这个结论很显然：当子串 s[i, j] 的长度等于 2 或者等于 3 的时候，我其实只需要判断一下头尾两个字符是否相等就可以直接下结论了。

如果子串 s[i + 1, j - 1] 只有 1 个字符，即去掉两头，剩下中间部分只有 11 个字符，当然是回文；
如果子串 s[i + 1, j - 1] 为空串，那么子串 s[i, j] 一定是回文子串。
因此，在 s[i] == s[j] 成立和 j - i < 3 的前提下，直接可以下结论，dp[i][j] = true，否则才执行状态转移。
