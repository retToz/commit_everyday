package com.wj.test.app;

import com.wj.test.app.game_system.PK;
import com.wj.test.app.game_system.Util;
import com.wj.test.app.role.UserRole;
import com.wj.test.app.game_system.User;

import java.util.ArrayList;

public class Start {
    public static void main() {

        /*
            > 1. 登录界面
            -登录
            -注册
            -退出
        */
        if(Util.LogIn()) return;
        /*  > 2. 游戏界面
            -分配点数：生命力 / 攻击力 / 防御力 共20点
            -战斗：随机匹配对手进行战斗
            -胜利可获得点数，选择进入下一关
            -失败退出游戏
        */
        System.out.println("\n\n");
        System.out.println("—————————————————————————————————");
        System.out.println("\t"+User.getUsername() + ", " + "欢迎来到游戏！\\(@^0^@)/\n");
        System.out.println("请先分配点数");

        // list[attack,defense,maxHp]
        ArrayList<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(0);
        list.add(0);
        // 初始时有20自由属性点,开始分配，打印分配结果
        Util.forAttribute(list, 20);
        System.out.println(list);
        /*
            1.定义一个怪物抽象类Monster
                属性：名称 / 生命值 hp / 攻击力 / 防御力
                方法：普攻，技能，被攻击
                （多定义几种怪物，且某些怪物有相同的能力，此时抽象一个接口，并且尝试用default实现后续功能）

            2.定义一个角色类
                属性：名称 / 等级 / 生命值 / 攻击力 / 防御力
                方法：普攻，技能，被攻击，升级，加点
        */

        // 把user，monster录入PK
        PK.user = new UserRole(User.getUsername(), list.get(0),list.get(1),list.get((2)));

        //随机匹配一只怪物
        PK.monster = Util.monsterChoose(PK.user.getLevel());

        // 进入对战平台
        String res = PK.pk(list);
        if(res.equals("win")){
            PK.nextPK(list);
        }


        // 游戏结束
        System.out.println("游戏结束了");



    }
}
