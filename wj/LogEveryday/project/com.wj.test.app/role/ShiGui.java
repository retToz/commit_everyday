package com.wj.test.app.role;

public class ShiGui extends Monster {
        /*
            尸鬼属性赋值逻辑
            1.level由永远比用户高1
            其他数值与level有关，具体关系为：
            attack = level * 20
            defense = level * 20
            maxHp = level * 80
         */

    public ShiGui() {
    }

    public ShiGui(int level) {
        // 默认基础数值attack 5， defense 5， maxHp 30
        super("尸鬼",level,5+level*20,5+level*20,30+level*80);
    }

    @Override
    public int skill1() {
        // 技能：挥砍 计算公式
        int attack = this.getAttack() * 1 + this.getLevel() * 5;
        System.out.println("尸鬼使用 挥砍 ！");
        return attack; // 返回攻击值
    }

    @Override
    public int skill2() {
        // 技能：野蛮砍击
        int attack = this.getAttack() * 5 + this.getLevel() * 10;
        System.out.println("尸鬼使用 野蛮砍击 ！你受到重创！ hp-"+attack);
        return attack; // 返回攻击值
    }
    public void printMessage(){
        System.out.println("尸鬼即将发起进攻！");
    }
}
