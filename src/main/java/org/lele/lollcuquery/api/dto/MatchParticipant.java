package org.lele.lollcuquery.api.dto;

public record MatchParticipant(
        int participantId,
        String puuid,
        String gameName,
        String tagLine,
        int teamId,
        int championId,
        int champLevel,
        int kills,
        int deaths,
        int assists,
        int goldEarned,
        int totalMinionsKilled,
        int item0,
        int item1,
        int item2,
        int item3,
        int item4,
        int item5,
        int item6,
        int spell1Id,
        int spell2Id,
        boolean win
) {
}
