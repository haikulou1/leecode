package 排序;

/**
 * Created by 胡歌的小迷弟 on 2019/10/8 20:16
 */
public class 希尔排序 {

    public static void main(String[] args) {
        int [] a={8,9,1,7,2,3,5,4,6,0};

        sort(a);
        for(int i=0;i<a.length;i++){
            System.out.println(a[i]);
        }
    }

    private static void sort(int[] arr) {
        for (int gap = arr.length / 2; gap > 0; gap /= 2) {
            // 从第gap个元素，逐个对其所在的组进行直接插入排序
            for (int i = gap; i < arr.length; i++) {
                int j = i;
                int temp = arr[j];
                if (arr[j] < arr[j - gap]) {
                    while (j - gap >= 0 && temp < arr[j - gap]) {
                        //移动
                        arr[j] = arr[j-gap];
                        j -= gap;
                    }
                    //当退出while后，就给temp找到插入的位置
                    arr[j] = temp;
                }

            }
        }

    }
}
