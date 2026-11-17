package com.wj.test.app.role;

public abstract class Role {
    /*
        定义一个角色类Role，包含以下属性：
        - 名称 name
        - 等级 level
        - 生命值 hp
        - 攻击力 attack
        - 防御力 defense
        方法：
        - 抽象方法：攻击 attack
        - 具体方法：被攻击 beAttacked
    */
    private String name;
    private int level;
    private int attack;
    private int defense;
    private int maxHp;
    private int hp;


    public Role() {
    }

    public Role(String name,int level, int attack, int defense, int maxHp) {
        this.name = name;
        this.level = level;
        this.attack = attack;
        this.defense = defense;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    // 技能
    public abstract int chooseSkillDescription();

    public int getLevel() {
        return level;
    }

    // 返回true代表战斗结束
    public boolean beAttacked(int attackPower) {
        int damage = attackPower - this.defense;
        if (damage > 0) {
            this.hp -= damage;
        }
        if(this.hp<=0){
            this.hp = 0;
            return true;
        }
        return false;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }
}
