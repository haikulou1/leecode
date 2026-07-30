package l04_median_of_two_sorted_arrays;

/**
 * Created by 胡歌的小迷弟 on 2019/12/23 21:41
 */


/**
 * There are two sorted arrays nums1 and nums2 of size m and n respectively.
 *
 * Find the median of the two sorted arrays. The overall run time complexity should be O(log (m+n)).
 *
 * You may assume nums1 and nums2 cannot be both empty.
 *
 * Example 1:
 *
 * nums1 = [1, 3]
 * nums2 = [2]
 *
 * The median is 2.0
 * Example 2:
 *
 * nums1 = [1, 2]
 * nums2 = [3, 4]
 *
 * The median is (2 + 3)/2 = 2.5
 */
public class Solution {

    public static void main(String[] args) {
        int a[]={1,3};
        int b[]={2};
        System.out.println(findMedianSortedArrays(a,b));
    }


    public static double findMedianSortedArrays(int[] nums1, int[] nums2) {
        double t=0.0;
        int result[]=new int[nums1.length+nums2.length];
        for(int i=0;i<nums1.length;i++){
            result[i]=nums1[i];
        }
        for(int i=nums1.length,j=0;i<nums1.length+nums2.length;i++){
            result[i]=nums2[j++];
        }
        sort(result,0,result.length-1);
        if(result.length%2==0){
            double t1=result[result.length/2];
            double t2=result[result.length/2-1];
            t=(t1+t2)/2;
            return t;
        }else{
            t=result[result.length/2];
            return t;
        }
    }


    private static void sort(int[] arr,int left,int right) {
        int l=left;
        int r=right;
        int pivot = arr[(left + right) / 2];
        int temp = 0; //临时变量，作为交换时使用
        while( l < r) {
            //在pivot的左边一直找,找到大于等于pivot值,才退出
            while( arr[l] < pivot) {
                l += 1;
            }
            //在pivot的右边一直找,找到小于等于pivot值,才退出
            while(arr[r] > pivot) {
                r -= 1;
            }
            //如果l >= r说明pivot 的左右两的值，已经按照左边全部是
            //小于等于pivot值，右边全部是大于等于pivot值
            if( l >= r) {
                break;
            }

            //交换
            temp = arr[l];
            arr[l] = arr[r];
            arr[r] = temp;

            //如果交换完后，发现这个arr[l] == pivot值 相等 r--， 前移
            if(arr[l] == pivot) {
                r -= 1;
            }
            //如果交换完后，发现这个arr[r] == pivot值 相等 l++， 后移
            if(arr[r] == pivot) {
                l += 1;
            }
        }

        // 如果 l == r, 必须l++, r--, 否则为出现栈溢出
        if (l == r) {
            l += 1;
            r -= 1;
        }
        //向左递归
        if(left < r) {
            sort(arr, left, r);
        }
        //向右递归
        if(right > l) {
            sort(arr, l, right);
        }
    }
}
