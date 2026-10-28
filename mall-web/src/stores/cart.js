import { defineStore } from 'pinia'
import { getCart, addCartItem, updateCartItem, removeCartItem } from '../api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({ items: [] }),
  getters: {
    totalCount: (s) => s.items.reduce((sum, i) => sum + i.quantity, 0),
    totalAmount: (s) => s.items.reduce((sum, i) => sum + Number(i.lineTotal), 0)
  },
  actions: {
    async fetchCart() {
      this.items = await getCart()
    },
    async addItem(productId, quantity = 1) {
      await addCartItem(productId, quantity)
      await this.fetchCart()
    },
    async updateQuantity(id, quantity) {
      await updateCartItem(id, quantity)
      await this.fetchCart()
    },
    async removeItem(id) {
      await removeCartItem(id)
      await this.fetchCart()
    }
  }
})
