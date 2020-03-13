package cn.wy.prototype;

/**
 * Created by 胡歌的小迷弟 on 2020/3/10 10:53
 */


import org.testng.annotations.Test;

import java.io.*;

/**
 *
 */
public class Myprototype {


    @Test
    public void testleepClone() throws CloneNotSupportedException {
        CloneTarget cloneTarget=new CloneTarget();
        cloneTarget.name="aaa";
        cloneTarget.person=new Person();
        cloneTarget.person.age="1";

        CloneTarget cloneTarget1= (CloneTarget) cloneTarget.clone();
        System.out.println(cloneTarget.person.age);



    }


    @Test
    public void testDeepClone() throws Exception {
        CloneTarget cloneTarget=new CloneTarget();
        cloneTarget.name="aaa";
        cloneTarget.person=new Person();
        cloneTarget.person.age="1";

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();

        ObjectOutputStream objectOutputStream=new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(cloneTarget);


        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        ObjectInputStream objectInputStream=new ObjectInputStream(byteArrayInputStream);



        CloneTarget cloneTarget11= (CloneTarget) objectInputStream.readObject();

        cloneTarget11.person.age="2";
        System.out.println(cloneTarget11.person.age);
        System.out.println(cloneTarget.person.age);
    }


}
