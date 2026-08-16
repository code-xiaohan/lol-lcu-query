package org.lele.lollcuquery.service;

import tools.jackson.databind.JsonNode;
import org.lele.lollcuquery.api.dto.RankedQueue;
import org.lele.lollcuquery.api.dto.SummonerSummary;
import org.lele.lollcuquery.lcu.LcuHttpClient;
import org.lele.lollcuquery.support.Jsons;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SummonerService {

    private static final Map<String, String> QUEUE_LABELS = Map.of(
            "RANKED_SOLO_5x5", "单双排",
            "RANKED_FLEX_SR", "灵活组排",
            "RANKED_TFT", "云顶之弈",
            "RANKED_FLEX_TT", "扭曲丛林"
    );

    private final LcuHttpClient lcuHttpClient;

    public SummonerService(LcuHttpClient lcuHttpClient) {
        this.lcuHttpClient = lcuHttpClient;
    }

    public SummonerSummary currentSummoner() {
        return toSummary(lcuHttpClient.get("/lol-summoner/v1/current-summoner"));
    }

    public Optional<SummonerSummary> findByPuuid(String puuid) {
        if (puuid == null || puuid.isBlank()) {
            return Optional.empty();
        }
        return lcuHttpClient.getOptional("/lol-summoner/v2/summoners/puuid/" + puuid)
                .or(() -> lcuHttpClient.getOptional("/lol-summoner/v1/summoners-by-puuid-cached/" + puuid))
                .map(this::toSummary);
    }

    public Optional<SummonerSummary> findByRiotId(String gameName, String tagLine) {
        if (gameName == null || gameName.isBlank()) {
            return Optional.empty();
        }
        String encodedName = URLEncoder.encode(gameName, StandardCharsets.UTF_8);
        String encodedTag = URLEncoder.encode(tagLine == null ? "" : tagLine, StandardCharsets.UTF_8);
        Optional<JsonNode> alias = lcuHttpClient.getOptional(
                "/lol-summoner/v1/alias/lookup?gameName=" + encodedName + "&tagLine=" + encodedTag);
        if (alias.isPresent()) {
            return Optional.of(toSummary(alias.get()));
        }
        return lcuHttpClient.getOptional("/lol-summoner/v1/summoners?name=" + encodedName)
                .map(this::toSummary);
    }

    public List<RankedQueue> rankedStats(String puuid) {
        JsonNode root = lcuHttpClient.get("/lol-ranked/v1/ranked-stats/" + puuid);
        JsonNode queueMap = root.path("queueMap");
        List<RankedQueue> queues = new ArrayList<>();
        if (queueMap.isObject()) {
            for (String key : queueMap.propertyNames()) {
                JsonNode node = queueMap.get(key);
                String tier = Jsons.text(node, "tier");
                if (tier.isBlank() || "NONE".equalsIgnoreCase(tier) || "NA".equalsIgnoreCase(tier)) {
                    continue;
                }
                queues.add(new RankedQueue(
                        key,
                        QUEUE_LABELS.getOrDefault(key, key),
                        tier,
                        Jsons.text(node, "division"),
                        Jsons.intVal(node, "leaguePoints"),
                        Jsons.intVal(node, "wins"),
                        Jsons.intVal(node, "losses")
                ));
            }
        }
        return queues;
    }

    public Optional<RankedQueue> soloRank(String puuid) {
        return rankedStats(puuid).stream()
                .filter(q -> "RANKED_SOLO_5x5".equals(q.queueType()))
                .findFirst();
    }

    public SummonerSummary toSummary(JsonNode node) {
        String gameName = Jsons.firstText(node, "gameName", "displayName", "summonerName", "name");
        String tagLine = Jsons.text(node, "tagLine");
        String displayName = tagLine.isBlank() ? gameName : gameName + "#" + tagLine;
        return new SummonerSummary(
                Jsons.text(node, "puuid"),
                Jsons.longVal(node, "summonerId"),
                gameName,
                tagLine,
                displayName,
                Jsons.intVal(node, "profileIconId"),
                Jsons.intVal(node, "summonerLevel")
        );
    }
}
