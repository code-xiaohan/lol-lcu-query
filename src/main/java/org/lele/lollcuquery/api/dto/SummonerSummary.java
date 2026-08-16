package org.lele.lollcuquery.api.dto;

public record SummonerSummary(
        String puuid,
        long summonerId,
        String gameName,
        String tagLine,
        String displayName,
        int profileIconId,
        int summonerLevel
) {
}
