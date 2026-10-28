import http from './http'

export function createOrder() {
  return http.post('/orders')
}

export function payOrder(id) {
  return http.post(`/orders/${id}/pay`)
}

export function getOrder(id) {
  return http.get(`/orders/${id}`)
}
