package com.wj.test.app.role;

import java.util.Random;
import java.util.Scanner;

public class UserRole extends Role implements Skills {
    // 用户角色类



    public UserRole() {
    }

    public UserRole(String name, int attack, int defense, int maxHp) {
        // 默认用户初始等级为 1 ，基础数值attack 5， defense 5， maxHp 120
        super(name, 1, 5 + attack*5, 5 + defense*5, 120 + maxHp*10);
    }

    // 升级加点
    public void levelUp(int level, int attack_num, int defense_num, int hp_num) {
        /*
        升级加点：用户升级后，有5点自由属性点，其中：
            attack每选中1点+5
            defense每选中1点+5
            hp每选中1点+10
        */
        this.setLevel(level);
        this.setAttack(this.getAttack() + attack_num * 5);
        this.setDefense(this.getDefense() + defense_num * 5);
        this.setMaxHp(this.getMaxHp() + hp_num * 10);
        this.setHp(this.getHp() + hp_num * 10);
    }

    @Override
    public int skill1() {
        // 技能：疯狂冲击
        /*
            消耗10hp值，对目标造成伤害
            1.如果血量不超过10，则消耗全部hp值
            2.血量充足，则消耗10hp值,并返回能造成的伤害
        */
        if (this.getHp() <= 10) return 0;
        else this.setHp(this.getHp() - 10);

        // 返回能对目标造成的伤害
        return this.getAttack() * 2 + new Random().nextInt(getLevel() * 5);
    }

    @Override
    public int skill2() {
        // 技能：治愈 消耗10hp回复生命值，回复量随着等级的提升而提升
        int addHp = new Random().nextInt(10 + this.getLevel() * 5, 40 + getLevel() * 5);

        // 如果 血量不超/10，则角色死亡；如果血量超过上限，则设置为上限
        if (getHp() <= 10) return 0;
        else if (getHp() + addHp > getMaxHp()) {
            setHp(getMaxHp());
            return getMaxHp();
        }
        else{
            setHp(getHp()+getHp() - 10 + addHp);
            return getHp() - 10 + addHp;
        }

    }

    @Override
    public int chooseSkillDescription() {
        // 技能描述:疯狂冲击;治愈
        System.out.println("—————————————————————————————————");
        System.out.println("|\t请选择您的技能:\t\t\t\t");
        System.out.println("|\t1.疯狂冲击:消耗10hp值，对目标造成伤害\t");
        System.out.println("|\t2.治愈：消耗恢复10hp\t\t\t");
        System.out.println("—————————————————————————————————");
        // 输入情况
        int attack_res = -1;
        switch (new Scanner(System.in).nextInt()) {
            case 1 -> {
                System.out.println("使用：疯狂冲击！");
                // 调用
                attack_res = skill1();
            }
            case 2 -> {
                int r = skill2();
                System.out.println("使用：治愈！正在回复 " + r + " 点生命值...");
                System.out.println("......");
            }
        }
        return attack_res;
    }
}
