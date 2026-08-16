package org.lele.lollcuquery.api.dto;

public record MatchSummary(
        long gameId,
        long gameCreation,
        int gameDuration,
        int queueId,
        String queueName,
        int championId,
        int kills,
        int deaths,
        int assists,
        boolean win,
        String role
) {
}
