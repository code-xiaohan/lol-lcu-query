package org.lele.lollcuquery.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.lele.lollcuquery.api.dto.LiveResponse;
import org.lele.lollcuquery.api.dto.LiveTeammate;
import org.lele.lollcuquery.api.dto.RankedQueue;
import org.lele.lollcuquery.api.dto.SummonerSummary;
import org.lele.lollcuquery.lcu.LcuHttpClient;
import org.lele.lollcuquery.support.Jsons;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class LiveSessionService {

    private final LcuHttpClient lcuHttpClient;
    private final SummonerService summonerService;

    public LiveSessionService(LcuHttpClient lcuHttpClient, SummonerService summonerService) {
        this.lcuHttpClient = lcuHttpClient;
        this.summonerService = summonerService;
    }

    public LiveResponse currentLive() {
        String phase = lcuHttpClient.getOptional("/lol-gameflow/v1/gameflow-phase")
                .map(node -> node.isTextual() ? node.asText() : node.toString().replace("\"", ""))
                .map(String::valueOf)
                .orElse("None");
        List<LiveTeammate> teammates = new ArrayList<>();
        if ("ChampSelect".equalsIgnoreCase(phase)) {
            teammates.addAll(fromChampSelect());
        } else if ("InProgress".equalsIgnoreCase(phase)
                || "GameStart".equalsIgnoreCase(phase)
                || "Reconnect".equalsIgnoreCase(phase)) {
            teammates.addAll(fromGameflow());
            if (teammates.isEmpty()) {
                teammates.addAll(fromLiveClientData());
            }
        }
        return new LiveResponse(phase, !teammates.isEmpty(), teammates);
    }

    private List<LiveTeammate> fromChampSelect() {
        JsonNode session = lcuHttpClient.getOptional("/lol-champ-select/v1/session").orElse(null);
        if (session == null) {
            return List.of();
        }
        List<LiveTeammate> teammates = new ArrayList<>();
        JsonNode myTeam = session.path("myTeam");
        if (myTeam.isArray()) {
            for (JsonNode member : myTeam) {
                toTeammate(
                        Jsons.text(member, "puuid"),
                        Jsons.longVal(member, "summonerId"),
                        Jsons.intVal(member, "championId"),
                        Jsons.firstText(member, "assignedPosition", "position"),
                        "",
                        ""
                ).ifPresent(teammates::add);
            }
        }
        return teammates;
    }

    private List<LiveTeammate> fromGameflow() {
        JsonNode session = lcuHttpClient.getOptional("/lol-gameflow/v1/session").orElse(null);
        if (session == null) {
            return List.of();
        }
        SummonerSummary me = summonerService.currentSummoner();
        JsonNode gameData = session.path("gameData");
        JsonNode teamOne = gameData.path("teamOne");
        JsonNode teamTwo = gameData.path("teamTwo");
        JsonNode myTeam = findMyTeam(teamOne, teamTwo, me);
        if (myTeam == null || !myTeam.isArray()) {
            return List.of();
        }
        List<LiveTeammate> teammates = new ArrayList<>();
        for (JsonNode member : myTeam) {
            toTeammate(
                    Jsons.firstText(member, "puuid"),
                    Jsons.longVal(member, "summonerId"),
                    Jsons.intVal(member, "championId"),
                    Jsons.firstText(member, "selectedPosition", "position"),
                    Jsons.firstText(member, "gameName", "summonerName"),
                    Jsons.text(member, "tagLine")
            ).ifPresent(teammates::add);
        }
        return teammates;
    }

    private List<LiveTeammate> fromLiveClientData() {
        JsonNode players = lcuHttpClient.getLiveClientOptional("/liveclientdata/playerlist").orElse(null);
        if (players == null || !players.isArray()) {
            return List.of();
        }
        SummonerSummary me = summonerService.currentSummoner();
        String myTeam = null;
        for (JsonNode player : players) {
            String gameName = Jsons.firstText(player, "riotIdGameName", "summonerName");
            if (me.gameName().equalsIgnoreCase(gameName)) {
                myTeam = Jsons.text(player, "team");
                break;
            }
        }
        if (myTeam == null || myTeam.isBlank()) {
            return List.of();
        }
        List<LiveTeammate> teammates = new ArrayList<>();
        for (JsonNode player : players) {
            if (!myTeam.equalsIgnoreCase(Jsons.text(player, "team"))) {
                continue;
            }
            String gameName = Jsons.firstText(player, "riotIdGameName", "summonerName");
            String tagLine = Jsons.text(player, "riotIdTagLine");
            Optional<SummonerSummary> summoner = summonerService.findByRiotId(gameName, tagLine);
            String puuid = summoner.map(SummonerSummary::puuid).orElse("");
            long summonerId = summoner.map(SummonerSummary::summonerId).orElse(0L);
            toTeammate(puuid, summonerId, 0, Jsons.text(player, "position"), gameName, tagLine)
                    .ifPresent(teammates::add);
        }
        return teammates;
    }

    private Optional<LiveTeammate> toTeammate(
            String puuid,
            long summonerId,
            int championId,
            String position,
            String fallbackName,
            String fallbackTag
    ) {
        SummonerSummary summary = null;
        if (puuid != null && !puuid.isBlank()) {
            summary = summonerService.findByPuuid(puuid).orElse(null);
        }
        if (summary == null && summonerId > 0) {
            summary = lcuHttpClient.getOptional("/lol-summoner/v1/summoners/" + summonerId)
                    .map(summonerService::toSummary)
                    .orElse(null);
        }
        if (summary == null && (fallbackName == null || fallbackName.isBlank()) && (puuid == null || puuid.isBlank())) {
            return Optional.empty();
        }
        String resolvedPuuid = summary != null ? summary.puuid() : (puuid == null ? "" : puuid);
        String gameName = summary != null ? summary.gameName() : fallbackName;
        String tagLine = summary != null ? summary.tagLine() : fallbackTag;
        RankedQueue solo = resolvedPuuid.isBlank() ? null : summonerService.soloRank(resolvedPuuid).orElse(null);
        return Optional.of(new LiveTeammate(
                resolvedPuuid,
                summary != null ? summary.summonerId() : summonerId,
                gameName,
                tagLine,
                championId,
                position == null ? "" : position.toUpperCase(Locale.ROOT),
                solo
        ));
    }

    private JsonNode findMyTeam(JsonNode teamOne, JsonNode teamTwo, SummonerSummary me) {
        if (containsMe(teamOne, me)) {
            return teamOne;
        }
        if (containsMe(teamTwo, me)) {
            return teamTwo;
        }
        return null;
    }

    private boolean containsMe(JsonNode team, SummonerSummary me) {
        if (team == null || !team.isArray()) {
            return false;
        }
        for (JsonNode member : team) {
            if (me.puuid().equals(Jsons.text(member, "puuid"))) {
                return true;
            }
            if (me.summonerId() > 0 && me.summonerId() == Jsons.longVal(member, "summonerId")) {
                return true;
            }
            if (me.gameName().equalsIgnoreCase(Jsons.firstText(member, "gameName", "summonerName"))) {
                return true;
            }
        }
        return false;
    }
}
