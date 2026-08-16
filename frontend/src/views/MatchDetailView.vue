<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import {
  api,
  championIcon,
  formatDuration,
  formatTime,
  itemIcon,
  type MatchDetail,
  type MatchParticipant
} from "../api";

const props = defineProps<{ gameId: string }>();
const route = useRoute();
const detail = ref<MatchDetail | null>(null);
const error = ref("");

const team100 = computed(() => detail.value?.participants.filter((p) => p.teamId === 100) ?? []);
const team200 = computed(() => detail.value?.participants.filter((p) => p.teamId === 200) ?? []);

function items(p: MatchParticipant): number[] {
  return [p.item0, p.item1, p.item2, p.item3, p.item4, p.item5, p.item6];
}

function displayName(p: MatchParticipant): string {
  return p.tagLine ? `${p.gameName}#${p.tagLine}` : p.gameName || "未知召唤师";
}

onMounted(async () => {
  try {
    detail.value = await api.matchDetail(props.gameId || String(route.params.gameId));
  } catch (err) {
    error.value = err instanceof Error ? err.message : "加载对局失败";
  }
});
</script>

<template>
  <section class="card">
    <p v-if="error" class="error">{{ error }}</p>
    <template v-else-if="detail">
      <div class="toolbar">
        <div>
          <h2 style="margin: 0">{{ detail.queueName }}</h2>
          <p class="muted">
            {{ formatTime(detail.gameCreation) }} · {{ formatDuration(detail.gameDuration) }} · {{ detail.gameMode }}
          </p>
        </div>
      </div>
      <div class="participants">
        <div>
          <h3 class="win">蓝色方</h3>
          <article v-for="p in team100" :key="p.participantId" class="participant">
            <img class="champion-icon sm" :src="championIcon(p.championId)" alt="" />
            <div>
              <div>{{ displayName(p) }}</div>
              <div class="muted">Lv.{{ p.champLevel }} · {{ p.totalMinionsKilled }} CS · {{ p.goldEarned }} 金</div>
            </div>
            <div>
              <div class="kda">{{ p.kills }}/{{ p.deaths }}/{{ p.assists }}</div>
              <div class="items">
                <img v-for="(item, index) in items(p)" :key="index" v-show="item" :src="itemIcon(item)" alt="" />
              </div>
            </div>
          </article>
        </div>
        <div>
          <h3 class="lose">红色方</h3>
          <article v-for="p in team200" :key="p.participantId" class="participant">
            <img class="champion-icon sm" :src="championIcon(p.championId)" alt="" />
            <div>
              <div>{{ displayName(p) }}</div>
              <div class="muted">Lv.{{ p.champLevel }} · {{ p.totalMinionsKilled }} CS · {{ p.goldEarned }} 金</div>
            </div>
            <div>
              <div class="kda">{{ p.kills }}/{{ p.deaths }}/{{ p.assists }}</div>
              <div class="items">
                <img v-for="(item, index) in items(p)" :key="index" v-show="item" :src="itemIcon(item)" alt="" />
              </div>
            </div>
          </article>
        </div>
      </div>
    </template>
    <p v-else class="muted">加载对局详情...</p>
  </section>
</template>
