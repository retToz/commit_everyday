package com.wj.test.app.role;

import java.util.Random;

public class Monster extends Role implements Skills {
    public Monster() {
    }

    public Monster(String name, int level, int attack, int defense, int maxHp) {
        super(name, level, attack, defense, maxHp);
    }

    @Override
    public int skill1() {
        System.out.println("这是 Monster 的 skill1() 方法");
        return 0;
    }

    @Override
    public int skill2() {
        System.out.println("这是 Monster 的 skill2() 方法");
        return 0;
    }

    @Override
    public int chooseSkillDescription() {

        Random r = new Random();
        int attack_res = -1;
        // monster 随机选择1个技能进行攻击，返回攻击值
        switch (r.nextInt(2)) {
            case 0 -> {
                attack_res = skill1();
            }
            case 1 -> {
                attack_res = skill2();
            }
        }
        return attack_res;
    }
}
