package com.wj.test.app.game_system;

import com.wj.test.app.role.Monster;
import com.wj.test.app.role.ShiGui;
import com.wj.test.app.role.ZhaoZe;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Util {



    static Random r = new Random();



    // 将分配的点数放进一个集合内返回
    public static ArrayList<Integer> forAttribute(ArrayList<Integer> list, int mount) {
        /*
            list存储attack，defense，maxHp
            mount表示拥有的点数
         */

        Scanner sc = new Scanner(System.in);

        while (mount > 0) {
            System.out.println("—————————————————————————————————");
            System.out.println("|\t当前剩余点数：" + mount + "     \t\t\t|");
            System.out.println("|\t1.攻击力  2.防御力  3.生命值\t|");
            System.out.println("—————————————————————————————————");
            int c = sc.nextInt();
            switch (c) {
                case 1 -> System.out.println("请输入攻击力:");
                case 2 -> System.out.println("请输入防御力:");
                case 3 -> System.out.println("请输入生命值:");
                default -> {
                    System.out.println("输入错误！请输入1  2  3 来分配");
                    continue;
                }
            }
            int num = sc.nextInt();
            System.out.println("\n");
            if (isRight(mount, num)) {
                System.out.println("分配成功! 当前属性点：");
                System.out.println("[攻击力："+list.get(0)+"  防御力："+list.get(1)+"  生命值"+list.get(1)+"]");
                // list[attack,defense,maxHp]
                mount -= num;
                list.set(c - 1, list.get(c - 1) + num);
            } else System.out.println("分配失败！请重新分配");
        }
        System.out.println("\n\n\n");
        System.out.println("—————————————————————————————————");
        System.out.println("所有属性点分配完成! \n [攻击力："+list.get(0)+"  防御力："+list.get(1)+"  生命值"+list.get(1)+"]");

        return list;
    }


    // 分配点数是否成功
    public static boolean isRight(int mount, int num) {
        if (num >= 0 && num <= mount) {
            return true;
        } else return false;
    }


    /*
     随机生成验证码，格式为6位，包含大小写字母以及1个数字
     先随机生成，然后打乱顺序
    */
    public static String getVerificationCode() {
        // 先创建一个字符数组，存放大小写字母
        char arr[] = new char[52];
        for (int i = 0; i < arr.length; i++) {
            if (i < 26)
                arr[i] = (char) ('a' + i);
            else
                arr[i] = (char) ('A' + i - 26);
        }
        // 随机生成一个0-9的数字
        char[] vc = new char[6];
        vc[0] = (char) (r.nextInt(10) + 48);

        for (int i = 0; i < 5; i++) {
            int index = r.nextInt(arr.length);
            vc[i + 1] = arr[index];
        }

        // 打乱顺序
        for (int i = 0; i < vc.length; i++) {
            int index = r.nextInt(vc.length);
            char temp = vc[i];
            vc[i] = vc[index];
            vc[index] = temp;

        }
        return new String(vc);
    }


    public static boolean LogIn() {
        // 设计登录界面
        Scanner input = new Scanner(System.in);
        boolean flag = false; // true 表示登陆成功，退出循环
        boolean isExit = false; // isExit = true 表示退出结束运行
        while (!flag) {
            System.out.println("\n\n");
            System.out.println("—————————————————————————————————");
            System.out.println("|    欢迎来到疯狂格斗游戏！👊    \t|");
            System.out.println("|                              \t|");
            System.out.println("| 请选择: 1. 登录 2. 注册 3. 退出\t|");
            System.out.println("—————————————————————————————————");
            // 登录标志

            switch (input.nextInt()) {
                case 1 -> {
                    for (int i = 1; i <= 3; i++) {
                        System.out.println("请输入用户名：");
                        String username = input.next();
                        System.out.println("请输入密码：");
                        String password = input.next();
                        String verificationCode = new String(getVerificationCode());
                        System.out.println("请输入验证码：" + verificationCode);
                        String vcInput = input.next();

                        System.out.println("\n\n");
                        if (username.equals(User.getUsername())
                                && password.equals(User.getPassword())
                                && vcInput.equals(verificationCode)) {
                            flag = true;
                            System.out.println("登录成功！");
                            break;
                        } else if (i <= 2) {
                            System.out.println("登录失败！剩余" + (3 - i) + "次机会，请重新输入");
                        } else {
                            System.out.println("登录失败！请联系客服！");

                        }
                    }
                }
                case 2 -> {
                    System.out.println("\n\n");
                    System.out.println("请输入用户名：");
                    String username = input.next();
                    User.setUsername(username);
                    System.out.println("请输入密码：");
                    String password = input.next();
                    User.setPassword(password);
                    System.out.println("注册成功！" + "您可以登录游戏了！");
                }
                case 3 -> {
                    System.out.println("退出");
                    isExit = true;
                    return isExit;
                }



            }
        }
        return false;
    }

    public static Monster monsterChoose(int userLevel){
        // 怪物匹配
        Monster ms = null;
        switch(r.nextInt(2)){
            case 0 -> {
                // 沼泽怪, 传入等级，初始化属性
                ms = new ZhaoZe(userLevel);
            }
            case 1 -> {
                // 尸鬼，传入等级，初始化属性
                ms = new ShiGui(userLevel);
            }
        }
        return ms;
    }
}
