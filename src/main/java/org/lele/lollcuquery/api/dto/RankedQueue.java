package org.lele.lollcuquery.api.dto;

public record RankedQueue(
        String queueType,
        String queueName,
        String tier,
        String division,
        int leaguePoints,
        int wins,
        int losses
) {
}
