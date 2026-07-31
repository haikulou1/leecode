package 数组;

import javax.xml.bind.SchemaOutputResolver;

/**
 * Created by 胡歌的小迷弟 on 2019/8/27 19:58
 */
public class 数组转稀疏数组1 {

    public static void main(String[] args) {
        int chessArr1[][]=new int[11][11];
        chessArr1[1][2]=1;
        chessArr1[3][4]=2;

        //1.先遍历二维数组，判断有效数据的个数

        int count=0;

        for(int i=0;i<11;i++){
            for(int j=0;j<11;j++){
                if(chessArr1[i][j]!=0){
                    count++;
                }
            }
        }

        int sparrse[][]=new int[count+1][3];
        sparrse[0][0]=11;
        sparrse[0][1]=11;
        sparrse[0][2]=count;

        int tmep=1;
        for(int i=0;i<11;i++){
            for(int j=0;j<11;j++){
                if(chessArr1[i][j]!=0){
                   sparrse[tmep][0]=i;
                   sparrse[tmep][1]=j;
                   sparrse[tmep][2]=chessArr1[i][j];
                   tmep++;
                }
            }
        }
        for(int i=0;i<count+1;i++){
            for(int j=0;j<3;j++){
                System.out.print(sparrse[i][j]+" ");
            }
            System.out.println(
            );
        }


    }
}
