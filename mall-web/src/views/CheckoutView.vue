<template>
  <div class="checkout">
    <h2>确认订单</h2>
    <el-table v-if="cart.items.length" :data="cart.items">
      <el-table-column prop="name" label="商品" />
      <el-table-column prop="quantity" label="数量" />
      <el-table-column prop="lineTotal" label="小计" />
    </el-table>
    <el-empty v-else description="购物车为空，无法结算" />
    <div class="footer" v-if="cart.items.length">
      <span>应付金额：¥ {{ cart.totalAmount.toFixed(2) }}</span>
      <el-button type="danger" :loading="paying" @click="pay">模拟支付</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { createOrder, payOrder } from '../api/order'

const cart = useCartStore()
const router = useRouter()
const paying = ref(false)

onMounted(() => cart.fetchCart())

async function pay() {
  paying.value = true
  try {
    const order = await createOrder()
    const paid = await payOrder(order.id)
    await cart.fetchCart()
    router.push(`/success/${paid.id}`)
  } catch (e) {
    // 错误提示由 http.js 响应拦截器统一弹出，这里仅保证按钮恢复可用
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
}
</style>
