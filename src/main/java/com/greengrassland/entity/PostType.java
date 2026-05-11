package com.greengrassland.entity;

/**
 * 活动类型枚举
 */
public enum PostType {
    /**
     * 打球
     */
    BALL_GAME("打球"),

    /**
     * 桌游
     */
    BOARD_GAME("桌游"),

    /**
     * 宠物社交
     */
    PET_SOCIAL("宠物社交"),

    /**
     * 拼活动
     */
    GROUP_ACTIVITY("拼活动"),

    /**
     * 学习小组
     */
    STUDY_GROUP("学习小组");

    private final String description;

    PostType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
