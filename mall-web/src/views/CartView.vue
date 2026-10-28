<template>
  <div>
    <h2>购物车</h2>
    <el-table :data="cart.items" v-if="cart.items.length">
      <el-table-column prop="name" label="商品" />
      <el-table-column prop="price" label="单价" />
      <el-table-column label="数量" width="200">
        <template #default="{ row }">
          <el-input-number :model-value="row.quantity" :min="1" @change="(v) => change(row, v)" />
        </template>
      </el-table-column>
      <el-table-column prop="lineTotal" label="小计" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" link @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else description="购物车为空" />
    <div class="footer" v-if="cart.items.length">
      <span>合计：¥ {{ cart.totalAmount.toFixed(2) }}</span>
      <el-button type="success" @click="$router.push('/checkout')">去结算</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useCartStore } from '../stores/cart'

const cart = useCartStore()

onMounted(() => cart.fetchCart())

async function change(row, value) {
  await cart.updateQuantity(row.id, value)
}

async function remove(row) {
  await cart.removeItem(row.id)
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
