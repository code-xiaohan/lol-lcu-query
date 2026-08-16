package org.lele.lollcuquery.api.dto;

import java.util.List;

public record MatchListResponse(String puuid, List<MatchSummary> matches) {
}
