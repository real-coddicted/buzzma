export default {
  async fetch(request, env) {
    const url = new URL(request.url)

    if (url.pathname.startsWith('/api/events/')) {
      const target = `${env.SSE_URL}${url.pathname}${url.search}`
      const sseHeaders = new Headers(request.headers)
      sseHeaders.delete('host')
      sseHeaders.delete('accept-encoding')
      const upstream = await fetch(target, {
        method: request.method,
        headers: sseHeaders,
        redirect: 'follow',
      })
      const responseHeaders = new Headers(upstream.headers)
      responseHeaders.delete('access-control-allow-origin')
      responseHeaders.delete('access-control-allow-credentials')
      responseHeaders.delete('access-control-allow-headers')
      responseHeaders.delete('access-control-allow-methods')
      return new Response(upstream.body, {
        status: upstream.status,
        headers: responseHeaders,
      })
    }

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
