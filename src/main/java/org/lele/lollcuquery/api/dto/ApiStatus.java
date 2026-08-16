package org.lele.lollcuquery.api.dto;

public record ApiStatus(boolean connected, String message, SummonerSummary summoner) {
}
