package com.wj.test.app.role;

public class ZhaoZe extends Monster {
    public ZhaoZe() {
    }

    public ZhaoZe(int level) {
        /*
            ZhaoZe怪属性赋值逻辑
            1.level由永远比用户高1
            其他数值与level有关，具体关系为：
            attack = level * 5
            defense = level * 10
            maxHp = level * 20
         */
        // 默认基础数值attack 5， defense 5， maxHp 30
        super("沼泽怪", level, 5 + level * 5, 5 + level * 10, 30 + level * 30);
    }

    @Override
    public int skill1() {
        // 技能：撞击 计算公式
        int attack = this.getAttack() * 1 + this.getLevel() * 5;
        System.out.println("沼泽怪使用 撞击 ！你擦伤了！hp-" + attack);
        return attack; // 返回攻击值
    }

    @Override
    public int skill2() {
        // 技能：粘液攻击
        int attack = this.getAttack() * 5 + this.getLevel() * 10;
        System.out.println("沼泽怪使用 粘液攻击 ！你感到伤口火辣！hp-" + attack);
        return attack; // 返回攻击值
    }

    public void printMessage() {
        System.out.println("沼泽怪即将发起进攻！");
    }
}
