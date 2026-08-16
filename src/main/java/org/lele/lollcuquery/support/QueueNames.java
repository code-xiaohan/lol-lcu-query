package org.lele.lollcuquery.support;

import java.util.Map;

public final class QueueNames {

    private static final Map<Integer, String> NAMES = Map.ofEntries(
            Map.entry(400, "匹配征召"),
            Map.entry(420, "单双排位"),
            Map.entry(430, "匹配模式"),
            Map.entry(440, "灵活排位"),
            Map.entry(450, "极地大乱斗"),
            Map.entry(480, "迅捷作战"),
            Map.entry(490, "快速匹配"),
            Map.entry(700, "冠军杯赛"),
            Map.entry(830, "人机入门"),
            Map.entry(840, "人机新手"),
            Map.entry(850, "人机一般"),
            Map.entry(900, "无限火力"),
            Map.entry(1020, "克隆大作战"),
            Map.entry(1300, "极限闪击"),
            Map.entry(1400, "终极魔典"),
            Map.entry(1700, "斗魂竞技场"),
            Map.entry(1900, "无限火力"),
            Map.entry(2000, "新手教程"),
            Map.entry(2010, "新手教程"),
            Map.entry(2020, "新手教程")
    );

    private QueueNames() {
    }

    public static String of(int queueId) {
        return NAMES.getOrDefault(queueId, "队列 " + queueId);
    }
}
