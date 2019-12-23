package 排序;

/**
 * Created by 胡歌的小迷弟 on 2019/9/25 16:12
 */
public class 直接插入排序 {

    public static void main(String[] args) {
        int a[]={3,2,5,6,1};
        insertSort(a);
        for(int i=0;i<a.length;i++){
            System.out.println(a[i]);
        }

    }

    public static void insertSort(int[] arr) {
        int insertVal = 0;
        int insertIndex = 0;
        //使用for循环来把代码简化
        for(int i=1;i<arr.length;i++){
            insertVal=arr[i];
            insertIndex=i-1;
            //找到插入的位置
            while(insertIndex>=0&&insertVal<arr[insertIndex]){
                arr[insertIndex+1]=arr[insertIndex];
                insertIndex--;
            }

            if(insertIndex!=i+1){
                arr[insertIndex+1]=insertVal;
            }

        }
    }
}
