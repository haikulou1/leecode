package 十种经典算法;

/**
 * Created by 胡歌的小迷弟 on 2019/12/4 21:38
 */
public class 二分查找 {

    public static void main(String[] args) {
        int arr[]={1,2,3,4,5,6};
        int i=digui(arr,3,0,5);
        System.out.println(i);
    }





//二分查找的非递归实现

    /**
     *
     * @param arr 待查找的数组
     * @param target 需要查找的值
     * @return
     */
    public static int binarySearch(int arr[],int target){

        int left=0;
        int right=arr.length-1;
        while(left<=right){
            int mid=(left+right)/2;
            if(arr[mid]==target){
                return mid;
            }else if(arr[mid]>target){
                right=mid-1;
            }else {
                left=mid+1;
            }
        }

        return -1;

    }


    //递归二分查找
    public static  int digui(int arr[],int target,int left,int right){
        int mid=(left+right)/2;
        if(left>right){
            return -1;
        }
        if(arr[mid]==target){
            return mid;
        }else  if(arr[mid]>target){
           return digui(arr,target,left,mid-1);
        }else {
            return  digui(arr,target,mid+1,right);
        }
    }
}