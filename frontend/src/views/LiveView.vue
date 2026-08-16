<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import {
  api,
  championIcon,
  formatDuration,
  formatRank,
  phaseLabel,
  type LiveResponse,
  type LiveTeammate,
  type MatchSummary
} from "../api";

const router = useRouter();
const live = ref<LiveResponse | null>(null);
const error = ref("");
const selected = ref<LiveTeammate | null>(null);
const teammateMatches = ref<MatchSummary[]>([]);
let timer = 0;

async function refresh() {
  try {
    live.value = await api.live();
    error.value = "";
  } catch (err) {
    error.value = err instanceof Error ? err.message : "加载当前对局失败";
  }
}

async function openTeammate(teammate: LiveTeammate) {
  selected.value = teammate;
  if (!teammate.puuid) {
    teammateMatches.value = [];
    return;
  }
  try {
    const data = await api.matches(teammate.puuid, 0, 9);
    teammateMatches.value = data.matches;
  } catch (err) {
    error.value = err instanceof Error ? err.message : "加载队友战绩失败";
  }
}

function openHistory(teammate: LiveTeammate) {
  if (!teammate.puuid) {
    return;
  }
  void router.push({ name: "matches", query: { puuid: teammate.puuid } });
}

onMounted(() => {
  void refresh();
  timer = window.setInterval(() => void refresh(), 4000);
});

onUnmounted(() => window.clearInterval(timer));
</script>

<template>
  <section class="card">
    <div class="toolbar">
      <div>
        <h2 style="margin: 0">当前对局</h2>
        <p class="muted">{{ phaseLabel[live?.phase || "None"] || live?.phase || "检测中" }}</p>
      </div>
      <button class="ghost" type="button" @click="refresh">刷新</button>
    </div>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="!live?.inLiveSession" class="muted">
      进入英雄选择或对局后，这里会显示己方队友。
    </p>
    <div v-else class="team-list">
      <article
        v-for="teammate in live.teammates"
        :key="teammate.puuid || teammate.gameName"
        class="teammate"
        @click="openTeammate(teammate)"
      >
        <img class="champion-icon sm" :src="championIcon(teammate.championId || -1)" alt="" />
        <div>
          <div>{{ teammate.gameName }}{{ teammate.tagLine ? "#" + teammate.tagLine : "" }}</div>
          <div class="muted">
            {{ teammate.assignedPosition || "未知位置" }} · {{ formatRank(teammate.soloRank) }}
          </div>
        </div>
        <button class="ghost" type="button" @click.stop="openHistory(teammate)">全部战绩</button>
      </article>
    </div>
  </section>

  <section v-if="selected" class="card drawer">
    <h3>{{ selected.gameName }} 的近期对局</h3>
    <div class="match-list">
      <article v-for="match in teammateMatches" :key="match.gameId" class="match-item">
        <img class="champion-icon sm" :src="championIcon(match.championId)" alt="" />
        <div>
          <div>{{ match.queueName }}</div>
          <div class="muted">{{ formatDuration(match.gameDuration) }}</div>
        </div>
        <div class="kda" :class="match.win ? 'win' : 'lose'">
          {{ match.win ? "胜" : "负" }} {{ match.kills }}/{{ match.deaths }}/{{ match.assists }}
        </div>
      </article>
      <p v-if="selected.puuid && !teammateMatches.length" class="muted">暂无战绩或该召唤师战绩不可查</p>
    </div>
  </section>
</template>
