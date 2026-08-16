export interface SummonerSummary {
  puuid: string;
  summonerId: number;
  gameName: string;
  tagLine: string;
  displayName: string;
  profileIconId: number;
  summonerLevel: number;
}

export interface RankedQueue {
  queueType: string;
  queueName: string;
  tier: string;
  division: string;
  leaguePoints: number;
  wins: number;
  losses: number;
}

export interface ApiStatus {
  connected: boolean;
  message: string;
  summoner: SummonerSummary | null;
}

export interface MeResponse {
  summoner: SummonerSummary;
  ranked: RankedQueue[];
}

export interface MatchSummary {
  gameId: number;
  gameCreation: number;
  gameDuration: number;
  queueId: number;
  queueName: string;
  championId: number;
  kills: number;
  deaths: number;
  assists: number;
  win: boolean;
  role: string;
}

export interface MatchListResponse {
  puuid: string;
  matches: MatchSummary[];
}

export interface MatchParticipant {
  participantId: number;
  puuid: string;
  gameName: string;
  tagLine: string;
  teamId: number;
  championId: number;
  champLevel: number;
  kills: number;
  deaths: number;
  assists: number;
  goldEarned: number;
  totalMinionsKilled: number;
  item0: number;
  item1: number;
  item2: number;
  item3: number;
  item4: number;
  item5: number;
  item6: number;
  spell1Id: number;
  spell2Id: number;
  win: boolean;
}

export interface MatchDetail {
  gameId: number;
  gameCreation: number;
  gameDuration: number;
  queueId: number;
  queueName: string;
  gameMode: string;
  participants: MatchParticipant[];
}

export interface LiveTeammate {
  puuid: string;
  summonerId: number;
  gameName: string;
  tagLine: string;
  championId: number;
  assignedPosition: string;
  soloRank: RankedQueue | null;
}

export interface LiveResponse {
  phase: string;
  inLiveSession: boolean;
  teammates: LiveTeammate[];
}

async function request<T>(path: string): Promise<T> {
  const response = await fetch(path);
  if (!response.ok) {
    let message = `请求失败 (${response.status})`;
    try {
      const body = await response.json();
      message = body.message || message;
    } catch {
      // ignore
    }
    throw new Error(message);
  }
  return response.json() as Promise<T>;
}

export const api = {
  status: () => request<ApiStatus>("/api/status"),
  me: () => request<MeResponse>("/api/me"),
  matches: (puuid?: string, begin = 0, end = 19) => {
    const params = new URLSearchParams({ begin: String(begin), end: String(end) });
    if (puuid) {
      params.set("puuid", puuid);
    }
    return request<MatchListResponse>(`/api/matches?${params.toString()}`);
  },
  matchDetail: (gameId: number | string) => request<MatchDetail>(`/api/matches/${gameId}`),
  live: () => request<LiveResponse>("/api/live")
};

export function championIcon(championId: number): string {
  return `https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/champion-icons/${championId}.png`;
}

export function profileIcon(iconId: number): string {
  return `https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/${iconId}.jpg`;
}

export function itemIcon(itemId: number): string {
  if (!itemId) {
    return "";
  }
  return `https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/associated-items/${itemId}.png`;
}

export function formatDuration(seconds: number): string {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins}:${String(secs).padStart(2, "0")}`;
}

export function formatTime(epochMs: number): string {
  if (!epochMs) {
    return "";
  }
  return new Date(epochMs).toLocaleString("zh-CN", { hour12: false });
}

export function formatRank(rank: RankedQueue | null | undefined): string {
  if (!rank || !rank.tier) {
    return "未定级";
  }
  return `${rank.tier} ${rank.division} ${rank.leaguePoints}LP`;
}

export const phaseLabel: Record<string, string> = {
  None: "空闲",
  Lobby: "房间",
  Matchmaking: "匹配中",
  ReadyCheck: "准备检查",
  ChampSelect: "英雄选择",
  GameStart: "游戏开始",
  InProgress: "对局中",
  Reconnect: "重连",
  WaitingForStats: "结算中",
  PreEndOfGame: "对局结束",
  EndOfGame: "对局结束"
};
