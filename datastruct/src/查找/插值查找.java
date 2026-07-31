package 查找;

/**
 * Created by 胡歌的小迷弟 on 2019/10/31 20:36
 */
public class 插值查找 {

    public static void main(String[] args) {
        int arr[]=new int[100];

        for(int i=0;i<100;i++){
            arr[i]=i;
        }

        System.out.println(insertSearch(arr,0,arr.length-1,99));
    }


    /**
     *
     * @param arr 数组
     * @param left 左边索引
     * @param right 右边索引
     * @param findVal 查找值
     * @return
     */
    public  static int insertSearch(int arr[],int left,int right,int findVal){
        //注意findVal<arr[left]||findVal>arr[right]必须需要，否则mid可能越界
        if(left>right|| findVal<arr[left]||findVal>arr[right]){
            return  -1;
        }

        int mid=left+(right-left)*(findVal-arr[left])/(arr[right]-arr[left]);

        int midVal=arr[mid];

        if(findVal>midVal){
            insertSearch(arr,mid+1,right,findVal);
        }else  if(midVal>findVal){
            insertSearch(arr,left,mid-1,findVal);
        }else {
            return mid;
        }
        return  -1;
    }
}
