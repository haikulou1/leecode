## 题目
You are given two non-empty linked lists representing two non-negative integers. The digits are stored in reverse order and each of their nodes contain a single digit. Add the two numbers and return it as a linked list.

You may assume the two numbers do not contain any leading zero, except the number 0 itself
```java
Input: (2 -> 4 -> 3) + (5 -> 6 -> 4)
Output: 7 -> 0 -> 8
Explanation: 342 + 465 = 807.
```
## 解题思路

第一种方法：

考虑到链表数字可能超过整型范围，使用BigDecimal,先将数据读入String，然后将String翻转，相加后继续翻转。

第二种方法：
直接将对应位置的数字相加，进位保存到一个临时变量