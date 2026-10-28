<template>
  <div>
    <el-row :gutter="16">
      <el-col v-for="p in products" :key="p.id" :span="6">
        <el-card class="card">
          <div class="name">{{ p.name }}</div>
          <div class="price">¥ {{ p.price }}</div>
          <div class="stock">库存 {{ p.stock }}</div>
          <el-button type="primary" :disabled="p.stock === 0" @click="add(p)">
            加入购物车
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProducts } from '../api/product'
import { useCartStore } from '../stores/cart'

const products = ref([])
const cart = useCartStore()

onMounted(async () => {
  products.value = await getProducts()
})

async function add(product) {
  await cart.addItem(product.id, 1)
  ElMessage.success('已加入购物车')
}
</script>

<style scoped>
.card { text-align: center; margin-bottom: 16px; }
.name { font-size: 16px; font-weight: 600; }
.price { color: #f56c6c; font-size: 18px; margin: 8px 0; }
.stock { color: #909399; margin-bottom: 8px; }
</style>
