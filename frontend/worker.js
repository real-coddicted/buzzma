export default {
  async fetch(request, env) {
    const url = new URL(request.url)

    if (url.pathname.startsWith('/api/')) {
      const target = `${env.BACKEND_URL}${url.pathname}${url.search}`
      return fetch(target, {
        method: request.method,
        headers: request.headers,
        body: ['GET', 'HEAD'].includes(request.method) ? undefined : request.body,
        redirect: 'follow',
      })
    }

    return env.ASSETS.fetch(request)
  },
}
