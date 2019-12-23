package 排序;

    /**
     * Created by 胡歌的小迷弟 on 2019/9/25 16:12
     */
    public class 冒泡排序 {

        public static void main(String[] args) {
            int a[]={3,9,-1,10,-2};
            sort(a);
            for(int i=0;i<a.length;i++){
                System.out.println(a[i]);
            }

        }

        static  int temp=0;
        public static void sort(int a[]){
            for(int i=0;i<a.length;i++){
                for(int j=0;j<a.length-i-1;j++){
                    if(a[j]>a[j+1]){
                        temp=a[j];
                        a[j]=a[j+1];
                        a[j+1]=temp;
                    }
                }
            }
        }
    }
