package com.health.agent.module.knowledge.enums;

/**
 * 文档处理状态
 */
public enum DocumentStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED;

    public static String toValue(DocumentStatus status) {
        return status.name().toLowerCase();
    }
}

