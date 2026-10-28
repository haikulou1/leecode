import http from './http'

export function getCart() {
  return http.get('/cart')
}

export function addCartItem(productId, quantity) {
  return http.post('/cart/items', { productId, quantity })
}

export function updateCartItem(id, quantity) {
  return http.put(`/cart/items/${id}`, { quantity })
}

export function removeCartItem(id) {
  return http.delete(`/cart/items/${id}`)
}
