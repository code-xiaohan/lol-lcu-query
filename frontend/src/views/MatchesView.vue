<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api, championIcon, formatDuration, formatTime, type MatchSummary } from "../api";

const route = useRoute();
const router = useRouter();
const matches = ref<MatchSummary[]>([]);
const error = ref("");
const loading = ref(false);

const puuid = () => (typeof route.query.puuid === "string" ? route.query.puuid : undefined);

async function load() {
  loading.value = true;
  try {
    const data = await api.matches(puuid());
    matches.value = data.matches;
    error.value = "";
  } catch (err) {
    error.value = err instanceof Error ? err.message : "加载战绩失败";
  } finally {
    loading.value = false;
  }
}

function open(gameId: number) {
  void router.push({ name: "match-detail", params: { gameId: String(gameId) } });
}

onMounted(() => void load());
</script>

<template>
  <section class="card">
    <div class="toolbar">
      <div>
        <h2 style="margin: 0">近期战绩</h2>
        <p class="muted">{{ puuid() ? "查看指定召唤师" : "当前登录召唤师" }}</p>
      </div>
      <button class="ghost" type="button" @click="load">刷新</button>
    </div>
    <p v-if="loading" class="muted">加载中...</p>
    <p v-else-if="error" class="error">{{ error }}</p>
    <div v-else class="match-list">
      <article
        v-for="match in matches"
        :key="match.gameId"
        class="match-item"
        @click="open(match.gameId)"
      >
        <img class="champion-icon sm" :src="championIcon(match.championId)" alt="" />
        <div>
          <div>{{ match.queueName }} · {{ match.role || "未知分路" }}</div>
          <div class="muted">{{ formatTime(match.gameCreation) }} · {{ formatDuration(match.gameDuration) }}</div>
        </div>
        <div style="text-align: right">
          <div class="kda" :class="match.win ? 'win' : 'lose'">
            {{ match.win ? "胜利" : "失败" }} {{ match.kills }}/{{ match.deaths }}/{{ match.assists }}
          </div>
        </div>
      </article>
      <p v-if="!matches.length" class="muted">暂无战绩</p>
    </div>
  </section>
</template>
