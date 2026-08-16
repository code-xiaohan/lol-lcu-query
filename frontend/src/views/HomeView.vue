<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import { api, formatRank, profileIcon, type ApiStatus, type MeResponse } from "../api";

const status = ref<ApiStatus | null>(null);
const me = ref<MeResponse | null>(null);
const error = ref("");
let timer = 0;

async function refresh() {
  try {
    status.value = await api.status();
    if (status.value.connected) {
      me.value = await api.me();
      error.value = "";
    } else {
      me.value = null;
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : "加载失败";
  }
}

onMounted(() => {
  void refresh();
  timer = window.setInterval(() => void refresh(), 4000);
});

onUnmounted(() => {
  window.clearInterval(timer);
});
</script>

<template>
  <section class="card">
    <div class="toolbar">
      <h2>客户端状态</h2>
      <button class="ghost" type="button" @click="refresh">刷新</button>
    </div>
    <p v-if="error" class="error">{{ error }}</p>
    <div v-else class="row">
      <span class="status-dot" :class="{ on: status?.connected }"></span>
      <div>
        <div>{{ status?.message || "正在检测客户端..." }}</div>
        <div class="muted">请先打开英雄联盟客户端并登录账号</div>
      </div>
    </div>
  </section>

  <section v-if="me" class="card" style="margin-top: 16px">
    <div class="row">
      <img class="profile-icon" :src="profileIcon(me.summoner.profileIconId)" alt="" />
      <div>
        <h2 style="margin: 0">{{ me.summoner.displayName }}</h2>
        <p class="muted">等级 {{ me.summoner.summonerLevel }}</p>
        <div class="row" style="flex-wrap: wrap">
          <span v-for="queue in me.ranked" :key="queue.queueType" class="badge">
            {{ queue.queueName }} · {{ formatRank(queue) }} · {{ queue.wins }}胜{{ queue.losses }}负
          </span>
          <span v-if="!me.ranked.length" class="badge">暂无排位数据</span>
        </div>
      </div>
    </div>
  </section>
</template>
