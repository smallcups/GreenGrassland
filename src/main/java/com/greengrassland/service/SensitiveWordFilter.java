package com.greengrassland.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class SensitiveWordFilter {

    private final Map<Character, Object> root = new HashMap<>();
    private static final String END_FLAG = "END";

    private static final Set<String> SENSITIVE_WORDS = Set.of(
        // 政治敏感词（示例）
        "法轮功", "falungong",
        // 色情/低俗
        "色情", "淫秽", "赌博", "裸聊",
        // 暴恐
        "恐怖主义", "杀人",
        // 诈骗/违法
        "毒品", "海洛因", "冰毒", "摇头丸", "代考", "替考", "办证", "假证",
        "高利贷", "裸贷", "校园贷",
        // 辱骂
        "傻逼", "妈的", "操你", "fuck", "shit",
        // 广告/引流
        "加微信", "加我微信", "扫码加", "免费送"
    );

    @PostConstruct
    public void init() {
        for (String word : SENSITIVE_WORDS) {
            addWord(word.toLowerCase());
        }
    }

    private void addWord(String word) {
        Map<Character, Object> node = root;
        for (char c : word.toCharArray()) {
            @SuppressWarnings("unchecked")
            Map<Character, Object> next = (Map<Character, Object>) node.computeIfAbsent(c, k -> new HashMap<>());
            node = next;
        }
        node.put(END_FLAG.charAt(0), true);
    }

    /**
     * 检查文本是否包含敏感词
     */
    public boolean containsSensitive(String text) {
        if (text == null || text.isEmpty()) return false;
        return findFirstMatch(text.toLowerCase()) != null;
    }

    /**
     * 返回匹配到的第一个敏感词
     */
    public String findFirstMatch(String text) {
        if (text == null || text.isEmpty()) return null;
        text = text.toLowerCase();
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> node = root;
            for (int j = i; j < text.length(); j++) {
                char c = text.charAt(j);
                @SuppressWarnings("unchecked")
                Map<Character, Object> next = (Map<Character, Object>) node.get(c);
                if (next == null) break;
                node = next;
                if (node.containsKey(END_FLAG.charAt(0))) {
                    return text.substring(i, j + 1);
                }
            }
        }
        return null;
    }

    /**
     * 替换敏感词为 *
     */
    public String replace(String text) {
        if (text == null || text.isEmpty()) return text;
        String result = text;
        String lower = text.toLowerCase();
        for (int i = 0; i < lower.length(); i++) {
            Map<Character, Object> node = root;
            int matchEnd = -1;
            for (int j = i; j < lower.length(); j++) {
                char c = lower.charAt(j);
                @SuppressWarnings("unchecked")
                Map<Character, Object> next = (Map<Character, Object>) node.get(c);
                if (next == null) break;
                node = next;
                if (node.containsKey(END_FLAG.charAt(0))) {
                    matchEnd = j;
                }
            }
            if (matchEnd >= 0) {
                StringBuilder sb = new StringBuilder(result);
                for (int k = i; k <= matchEnd; k++) {
                    sb.setCharAt(k, '*');
                }
                result = sb.toString();
                i = matchEnd;
            }
        }
        return result;
    }
}
