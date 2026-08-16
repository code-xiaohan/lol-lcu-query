<script setup lang="ts">
import { RouterLink, RouterView } from "vue-router";
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <div class="brand">
        <strong>LOL 战绩查询</strong>
        <span>基于本机 League 客户端 LCU</span>
      </div>
      <nav class="nav">
        <RouterLink to="/">概览</RouterLink>
        <RouterLink to="/matches">战绩</RouterLink>
        <RouterLink to="/live">当前对局</RouterLink>
      </nav>
    </header>
    <main class="page">
      <RouterView />
    </main>
  </div>
</template>
