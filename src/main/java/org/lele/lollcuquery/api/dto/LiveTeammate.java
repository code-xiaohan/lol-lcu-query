package org.lele.lollcuquery.api.dto;

public record LiveTeammate(
        String puuid,
        long summonerId,
        String gameName,
        String tagLine,
        int championId,
        String assignedPosition,
        RankedQueue soloRank
) {
}
