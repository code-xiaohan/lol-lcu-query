package org.lele.lollcuquery.api;

import org.lele.lollcuquery.api.dto.ApiStatus;
import org.lele.lollcuquery.api.dto.LiveResponse;
import org.lele.lollcuquery.api.dto.MatchDetail;
import org.lele.lollcuquery.api.dto.MatchListResponse;
import org.lele.lollcuquery.api.dto.MeResponse;
import org.lele.lollcuquery.lcu.LcuConnection;
import org.lele.lollcuquery.service.LiveSessionService;
import org.lele.lollcuquery.service.MatchService;
import org.lele.lollcuquery.service.SummonerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QueryController {

    private final LcuConnection connection;
    private final SummonerService summonerService;
    private final MatchService matchService;
    private final LiveSessionService liveSessionService;

    public QueryController(
            LcuConnection connection,
            SummonerService summonerService,
            MatchService matchService,
            LiveSessionService liveSessionService
    ) {
        this.connection = connection;
        this.summonerService = summonerService;
        this.matchService = matchService;
        this.liveSessionService = liveSessionService;
    }

    @GetMapping("/status")
    public ApiStatus status() {
        if (!connection.isConnected()) {
            return new ApiStatus(false, "未检测到英雄联盟客户端", null);
        }
        try {
            return new ApiStatus(true, "已连接", summonerService.currentSummoner());
        } catch (Exception ex) {
            return new ApiStatus(false, "客户端已启动但尚未登录完成", null);
        }
    }

    @GetMapping("/me")
    public MeResponse me() {
        var summoner = summonerService.currentSummoner();
        return new MeResponse(summoner, summonerService.rankedStats(summoner.puuid()));
    }

    @GetMapping("/matches")
    public MatchListResponse matches(
            @RequestParam(required = false) String puuid,
            @RequestParam(defaultValue = "0") int begin,
            @RequestParam(defaultValue = "19") int end
    ) {
        return matchService.listMatches(puuid, begin, end);
    }

    @GetMapping("/matches/{gameId}")
    public MatchDetail matchDetail(@PathVariable long gameId) {
        return matchService.matchDetail(gameId);
    }

    @GetMapping("/live")
    public LiveResponse live() {
        return liveSessionService.currentLive();
    }
}
