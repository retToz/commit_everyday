package com.wj.test.app.game_system;

import com.wj.test.app.role.Monster;
import com.wj.test.app.role.ShiGui;
import com.wj.test.app.role.UserRole;
import com.wj.test.app.role.ZhaoZe;

import java.util.ArrayList;
import java.util.Scanner;

import static com.wj.test.app.game_system.Util.forAttribute;

public class PK {
    /*
        这是一个战斗类，User与Monster进行pk，最终返回结果
        属性：
            1.静态初始化User，Monster对象
        方法：
            1.pk()方法，返回结果
    */

    public static UserRole user;
    public static Monster monster;

    public PK() {
    }

    public PK(UserRole user, Monster monster) {
        this.user = user;
        this.monster = monster;
    }

    public static boolean isZhaoZe(Monster monster) {
        if (monster instanceof ZhaoZe)
            return true;
        else
            return false;
    }

    public static boolean isShiGui(Monster monster) {
        if (monster instanceof ShiGui)
            return true;
        else
            return false;
    }

    // 进行下一场PK或结束
    public static void nextPK(ArrayList<Integer> list) {
        boolean pkFlag = true;
        while (pkFlag) {

            System.out.println("恭喜您获胜了！是否开启新战局？");
            System.out.println("[y/n]");

            //输入 y or n
            Scanner sc = new Scanner(System.in);
            String str = sc.next();
            str.trim();

            //不是y or n，告诉用户重新输入
            if (str.length() != 1 || (str.charAt(0) != 'y' && str.charAt(0) != 'n'))
                System.out.println("输入错误，请重新输入！");
            else if (str.charAt(0) == 'n') {
                System.out.println("退出游戏");
                return;
            } else if (str.charAt(0) == 'y') {
                /*
                    1.升级，保持上一轮的属性，获得可分配的属性点
                    2.再次pk
                */

                System.out.println("恭喜你，升级了！现在可以分配点数！");
                // 拷贝list副本
                ArrayList<Integer> old_list = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    old_list.add(list.get(i));
                }
                // 每升一级获得5点属性
                forAttribute(list, 5);
                // 升级
                PK.user.levelUp(user.getLevel()+1,list.get(0) - old_list.get(0), list.get(1) - old_list.get(1), list.get(2) - old_list.get(2));
                // 重新匹配怪物，怪物随玩家等级提升
                PK.monster = Util.monsterChoose(PK.user.getLevel());
                // 新一轮pk
                String res = PK.pk(list);
                // 当匹配不到win终止
                if(!res.equals("win")) {
                    pkFlag = false;
                }

            } else {
                System.out.println("其他输入错误...");
                return;
            }


        }


    }


    // 对战采用回合制：每次循环用户先发起攻击，随后怪物发起攻击，当其中一方hp降至0结束，返回对战结果
    public static String pk(ArrayList<Integer> list) {

        int times = 1;
        boolean isEnd = false;
        while (!isEnd) {
            // user attack
            System.out.println("\n\n==================第" + times + "回合开始了!====================");
            System.out.println(user.getName() + " HP:" + user.getHp() + "\t" + monster.getName() + "HP:" + monster.getHp());
            times++;
            int attack_res = user.chooseSkillDescription();
            if (attack_res == -1) System.out.println("治疗成功！");
            else {
                // 生命值不足强行使用技能
                if (user.getHp() <= 10) {
                    System.out.println("you dead......");
                    System.out.println("你为错误的抉择付出了代价......");
                    isEnd = true;
                    continue;
                }

                isEnd = monster.beAttacked(attack_res);
                if (isEnd) {
                    System.out.println(monster.getName() + "hp-"+attack_res+", 死亡！");
                    break;
                } else
                    System.out.println(monster.getName() + "受到了重创！hp-" + attack_res + "剩余血量为：" + monster.getHp());
            }


            // monster attack(怪物直接给两个攻击技能即可)

            if (isZhaoZe(monster)) {
                ZhaoZe zz = (ZhaoZe) monster;
                zz.printMessage();
                attack_res= zz.chooseSkillDescription();
                isEnd = user.beAttacked(attack_res);
                }
            if (isShiGui(monster)) {
                ShiGui sg = (ShiGui) monster;
                sg.printMessage();
                attack_res= sg.chooseSkillDescription();
                isEnd = user.beAttacked(attack_res);
            }
            if (isEnd) {
                System.out.println("你受到重创！ hp-"+attack_res+" 角色已死亡！");
                break;
            }
            else System.out.println("你受伤了！ hp-"+attack_res+" 你的剩余血量为：" + user.getHp());
        }

//        if(res.equals("您胜利了！是否需要进行下一场对决？"))
//

        if (user.getHp() <= 0)
            return "您阵亡了，挑战失败！";
        else if (monster.getHp() <= 0 && user.getHp()>0) {
            //当怪物死亡，角色仍存活时，用户可以进行下一场战斗，也可选择退出
            return "win";
        }

        return "error";
    }

}
