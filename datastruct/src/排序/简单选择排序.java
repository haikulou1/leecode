package 排序;

/**
 * Created by 胡歌的小迷弟 on 2019/9/25 16:12
 */
public class 简单选择排序 {

    public static void main(String[] args) {
        int a[]={8,3,2,1,7,4,6,5};
        sort(a);
        for(int i=0;i<a.length;i++){
            System.out.println(a[i]);
        }

    }

    static  int index=0;
    static  int tmep=0;
    public static void sort(int a[]){
        for(int i=0;i<a.length;i++){
            index=i;
            for(int j=i+1;j<a.length;j++){
                if(a[j]<a[index]){
                    index=j;
                }
                if(index!=i){
                    tmep=a[i];
                    a[i]=a[index];
                    a[index]=tmep;
                }

            }
        }
    }
}
