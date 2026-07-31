package 树;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 胡歌的小迷弟 on 2019/11/25 19:16
 */
public class 哈夫曼树 {
    public static void main(String[] args) {
        int arr[] = { 13, 7, 8, 3, 29, 6, 1 };
        Nodes root = createHuffmanTree(arr);

        //测试一把
        preOrder(root); //

    }

    //编写一个前序遍历的方法
    public static void preOrder(Nodes root) {
        if(root != null) {
            root.preOrder();
        }else{
            System.out.println("是空树，不能遍历~~");
        }
    }

    // 创建赫夫曼树的方法
    /**
     *
     * @param arr 需要创建成哈夫曼树的数组
     * @return 创建好后的赫夫曼树的root结点
     */
    public static Nodes createHuffmanTree(int[] arr) {
        // 第一步为了操作方便
        // 1. 遍历 arr 数组
        // 2. 将arr的每个元素构成成一个Node
        // 3. 将Node 放入到ArrayList中
        List<Nodes> nodes = new ArrayList<Nodes>();
        for (int value : arr) {
            nodes.add(new Nodes(value));
        }

        //我们处理的过程是一个循环的过程


        while(nodes.size() > 1) {

            //排序 从小到大
            Collections.sort(nodes);


            //取出根节点权值最小的两颗二叉树
            //(1) 取出权值最小的结点（二叉树）
            Nodes leftNode = nodes.get(0);
            //(2) 取出权值第二小的结点（二叉树）
            Nodes rightNode = nodes.get(1);

            //(3)构建一颗新的二叉树
            Nodes parent = new Nodes(leftNode.value + rightNode.value);
            parent.left = leftNode;
            parent.right = rightNode;

            //(4)从ArrayList删除处理过的二叉树
            nodes.remove(leftNode);
            nodes.remove(rightNode);
            //(5)将parent加入到nodes
            nodes.add(parent);
        }

        //返回哈夫曼树的root结点
        return nodes.get(0);

    }
}


class Nodes implements Comparable<Nodes>{
    int value;
    Nodes left;

    @Override
    public String toString() {
        return "Nodes{" +
                "value=" + value +
                '}';
    }

    public Nodes(int value) {
        this.value = value;
    }

    Nodes right;

    @Override
    public int compareTo(Nodes o) {
        return this.value-o.value;//从小到大排序
    }
    //编写前序遍历的方法
    public void preOrder() {
        System.out.println(this); //先输出父结点
        //递归向左子树前序遍历
        if(this.left != null) {
            this.left.preOrder();
        }
        //递归向右子树前序遍历
        if(this.right != null) {
            this.right.preOrder();
        }
    }
}
