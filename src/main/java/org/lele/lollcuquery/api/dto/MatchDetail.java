package org.lele.lollcuquery.api.dto;

import java.util.List;

public record MatchDetail(
        long gameId,
        long gameCreation,
        int gameDuration,
        int queueId,
        String queueName,
        String gameMode,
        List<MatchParticipant> participants
) {
}
