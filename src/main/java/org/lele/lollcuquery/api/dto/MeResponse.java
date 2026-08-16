package org.lele.lollcuquery.api.dto;

import java.util.List;

public record MeResponse(SummonerSummary summoner, List<RankedQueue> ranked) {
}
