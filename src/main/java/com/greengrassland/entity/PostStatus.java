package com.greengrassland.entity;

public enum PostStatus {
    RECRUITING("招募中"),
    FULL("已满员"),
    ONGOING("进行中"),
    FINISHED("已结束"),
    CANCELLED("已取消");

    private final String description;

    PostStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
