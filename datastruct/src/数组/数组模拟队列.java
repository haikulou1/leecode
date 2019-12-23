package 数组;

import javax.xml.bind.SchemaOutputResolver;
import java.util.Queue;

/**
 * Created by 胡歌的小迷弟 on 2019/8/27 20:11
 */
public class 数组模拟队列 {

    public static void main(String[] args) {

    }



}


class ArrayQueue{

    private int maxSize;
    private int front;
    private  int rear;
    private int[] arr;


    public ArrayQueue(int maxSize){
        this.maxSize=maxSize;
        arr=new int[maxSize];
        front=-1;
        rear=-1;
    }

    public boolean isFull(){

       return rear==maxSize-1;

    }

    public boolean isEmpty(){
        return front==rear;
    }

    public void addQueue(int data){
        if(isFull()){
            return;
        }else {
            rear++;
            arr[rear]=data;
        }
    }

    public int getQueue(){

        if(isEmpty()){
            return -1;
        }else
        {
            front++;
            return arr[front];
        }
    }


    public int headQueue(){

        if(isEmpty()){
            return -1;
        }else
        {
            return arr[front+1];
        }
    }

    public  void showQueue(){
        if(isEmpty()){
            return;
        }

        for(int i=0;i<arr.length;i++){
            System.out.println(arr[i]);
        }
    }
}