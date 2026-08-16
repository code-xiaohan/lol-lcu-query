import { createRouter, createWebHashHistory } from "vue-router";
import HomeView from "./views/HomeView.vue";
import MatchesView from "./views/MatchesView.vue";
import MatchDetailView from "./views/MatchDetailView.vue";
import LiveView from "./views/LiveView.vue";

export const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/", name: "home", component: HomeView },
    { path: "/matches", name: "matches", component: MatchesView },
    { path: "/matches/:gameId", name: "match-detail", component: MatchDetailView, props: true },
    { path: "/live", name: "live", component: LiveView }
  ]
});
