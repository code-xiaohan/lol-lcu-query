package org.lele.lollcuquery.api.dto;

import java.util.List;

public record LiveResponse(String phase, boolean inLiveSession, List<LiveTeammate> teammates) {
}
