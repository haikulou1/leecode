<template>
  <el-result v-if="order" icon="success" title="支付成功" sub-title="订单已完成">
    <template #extra>
      <p>订单号：{{ order.orderNo }}</p>
      <p>支付金额：¥ {{ Number(order.totalAmount).toFixed(2) }}</p>
      <el-button type="primary" @click="$router.push('/')">继续购物</el-button>
    </template>
  </el-result>
  <el-result v-else-if="error" icon="error" title="订单加载失败" :sub-title="error">
    <template #extra>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </template>
  </el-result>
  <el-skeleton v-else :rows="3" animated />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getOrder } from '../api/order'

const route = useRoute()
const order = ref(null)
const error = ref('')

onMounted(async () => {
  try {
    order.value = await getOrder(route.params.orderId)
  } catch (e) {
    error.value = e?.message || '订单不存在'
  }
})
</script>
