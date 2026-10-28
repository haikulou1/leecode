<template>
  <el-container>
    <el-header class="header">
      <router-link to="/" class="brand">电商 Demo</router-link>
      <el-button type="primary" @click="$router.push('/cart')">购物车</el-button>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
}
.brand {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  text-decoration: none;
}
</style>
