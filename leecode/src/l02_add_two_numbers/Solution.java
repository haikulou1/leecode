package l02_add_two_numbers;

/**
 * Created by 胡歌的小迷弟 on 2019/12/23 21:41
 */

import java.math.BigDecimal;

public class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode node1=new ListNode(0);
        String str1="";
        String str2="";
        while(l1!=null){
            str1+=l1.val;
            l1=l1.next;
        }
        while(l2!=null){
            str2+=l2.val;
            l2=l2.next;
        }
        str1=swap(str1);
        str2=swap(str2);
        BigDecimal str3=new BigDecimal(str1);
        BigDecimal str4=new BigDecimal(str2);;
        BigDecimal p=str3.add(str4);
        String result=swap(p.toString());
        ListNode temp=node1;
        for(int i=0;i<result.length();i++){
            ListNode node=new ListNode(result.charAt(i)-'0');
            temp.next=node;
            temp=node;

        }
        return node1.next;
    }


    /**
     * \第二种方法
     * @param l1
     * @param l2
     * @return
     */
    public ListNode addTwoNumbers2(ListNode l1, ListNode l2) {
        ListNode dummyHead = new ListNode(0);
        ListNode p = l1, q = l2, curr = dummyHead;
        int carry = 0;
        while (p != null || q != null) {
            int x = (p != null) ? p.val : 0;
            int y = (q != null) ? q.val : 0;
            int sum = carry + x + y;
            carry = sum / 10;
            curr.next = new ListNode(sum % 10);
            curr = curr.next;
            if (p != null) p = p.next;
            if (q != null) q = q.next;
        }
        if (carry > 0) {
            curr.next = new ListNode(carry);
        }
        return dummyHead.next;
    }

    public String swap(String str){
        char c[]=str.toCharArray();
        char a;
        for(int i=0;i<str.length()/2;i++){
            a=c[str.length()-1-i];
            c[str.length()-1-i]=c[i];
            c[i]=a;
        }
        return  String.valueOf(c);
    }

    class ListNode {
        int val;
        ListNode next;
        ListNode(int x) { val = x; }
    }
}



