package org.lele.lollcuquery.service;

import tools.jackson.databind.JsonNode;
import org.lele.lollcuquery.api.dto.MatchDetail;
import org.lele.lollcuquery.api.dto.MatchListResponse;
import org.lele.lollcuquery.api.dto.MatchParticipant;
import org.lele.lollcuquery.api.dto.MatchSummary;
import org.lele.lollcuquery.lcu.LcuHttpClient;
import org.lele.lollcuquery.support.Jsons;
import org.lele.lollcuquery.support.QueueNames;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchService {

    private final LcuHttpClient lcuHttpClient;
    private final SummonerService summonerService;

    public MatchService(LcuHttpClient lcuHttpClient, SummonerService summonerService) {
        this.lcuHttpClient = lcuHttpClient;
        this.summonerService = summonerService;
    }

    public MatchListResponse listMatches(String puuid, int begin, int end) {
        String targetPuuid = resolvePuuid(puuid);
        int safeBegin = Math.max(begin, 0);
        int safeEnd = Math.max(end, safeBegin + 1);
        JsonNode root = lcuHttpClient.get(
                "/lol-match-history/v1/products/lol/" + targetPuuid + "/matches?begIndex=" + safeBegin + "&endIndex=" + safeEnd);
        JsonNode games = root.path("games").path("games");
        List<MatchSummary> summaries = new ArrayList<>();
        if (games.isArray()) {
            for (JsonNode game : games) {
                summaries.add(toSummary(game, targetPuuid));
            }
        }
        return new MatchListResponse(targetPuuid, summaries);
    }

    public MatchDetail matchDetail(long gameId) {
        JsonNode game = lcuHttpClient.get("/lol-match-history/v1/games/" + gameId);
        return toDetail(game);
    }

    private String resolvePuuid(String puuid) {
        if (puuid != null && !puuid.isBlank()) {
            return puuid;
        }
        return summonerService.currentSummoner().puuid();
    }

    private MatchSummary toSummary(JsonNode game, String puuid) {
        MatchParticipant self = findSelf(game, puuid);
        JsonNode timeline = findParticipantNode(game, puuid).path("timeline");
        String role = Jsons.firstText(timeline, "lane", "role");
        int queueId = Jsons.intVal(game, "queueId");
        return new MatchSummary(
                Jsons.longVal(game, "gameId"),
                Jsons.longVal(game, "gameCreation"),
                Jsons.intVal(game, "gameDuration"),
                queueId,
                QueueNames.of(queueId),
                self.championId(),
                self.kills(),
                self.deaths(),
                self.assists(),
                self.win(),
                role
        );
    }

    private MatchDetail toDetail(JsonNode game) {
        int queueId = Jsons.intVal(game, "queueId");
        return new MatchDetail(
                Jsons.longVal(game, "gameId"),
                Jsons.longVal(game, "gameCreation"),
                Jsons.intVal(game, "gameDuration"),
                queueId,
                QueueNames.of(queueId),
                Jsons.text(game, "gameMode"),
                toParticipants(game)
        );
    }

    private List<MatchParticipant> toParticipants(JsonNode game) {
        Map<Integer, JsonNode> identities = new HashMap<>();
        JsonNode identityNodes = game.path("participantIdentities");
        if (identityNodes.isArray()) {
            for (JsonNode identity : identityNodes) {
                identities.put(Jsons.intVal(identity, "participantId"), identity.path("player"));
            }
        }
        List<MatchParticipant> participants = new ArrayList<>();
        JsonNode participantNodes = game.path("participants");
        if (participantNodes.isArray()) {
            for (JsonNode participant : participantNodes) {
                int participantId = Jsons.intVal(participant, "participantId");
                JsonNode player = identities.getOrDefault(participantId, participant.path("player"));
                JsonNode stats = participant.has("stats") ? participant.path("stats") : participant;
                String gameName = Jsons.firstText(player, "gameName", "summonerName", "riotIdGameName");
                String tagLine = Jsons.firstText(player, "tagLine", "riotIdTagLine");
                participants.add(new MatchParticipant(
                        participantId,
                        Jsons.firstText(player, "puuid"),
                        gameName,
                        tagLine,
                        Jsons.intVal(participant, "teamId"),
                        Jsons.intVal(participant, "championId"),
                        Jsons.intVal(stats, "champLevel"),
                        Jsons.intVal(stats, "kills"),
                        Jsons.intVal(stats, "deaths"),
                        Jsons.intVal(stats, "assists"),
                        Jsons.intVal(stats, "goldEarned"),
                        Jsons.intVal(stats, "totalMinionsKilled") + Jsons.intVal(stats, "neutralMinionsKilled"),
                        Jsons.intVal(stats, "item0"),
                        Jsons.intVal(stats, "item1"),
                        Jsons.intVal(stats, "item2"),
                        Jsons.intVal(stats, "item3"),
                        Jsons.intVal(stats, "item4"),
                        Jsons.intVal(stats, "item5"),
                        Jsons.intVal(stats, "item6"),
                        Jsons.intVal(participant, "spell1Id"),
                        Jsons.intVal(participant, "spell2Id"),
                        Jsons.boolVal(stats, "win")
                ));
            }
        }
        return participants;
    }

    private MatchParticipant findSelf(JsonNode game, String puuid) {
        return toParticipants(game).stream()
                .filter(p -> puuid.equals(p.puuid()))
                .findFirst()
                .orElseGet(() -> new MatchParticipant(0, puuid, "", "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false));
    }

    private JsonNode findParticipantNode(JsonNode game, String puuid) {
        JsonNode identities = game.path("participantIdentities");
        int participantId = -1;
        if (identities.isArray()) {
            for (JsonNode identity : identities) {
                if (puuid.equals(Jsons.text(identity.path("player"), "puuid"))) {
                    participantId = Jsons.intVal(identity, "participantId");
                    break;
                }
            }
        }
        JsonNode participants = game.path("participants");
        if (participants.isArray()) {
            for (JsonNode participant : participants) {
                if (participantId > 0 && Jsons.intVal(participant, "participantId") == participantId) {
                    return participant;
                }
                if (puuid.equals(Jsons.text(participant, "puuid"))) {
                    return participant;
                }
            }
        }
        return game;
    }
}
