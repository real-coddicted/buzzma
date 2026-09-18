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

    const assetResponse = await env.ASSETS.fetch(request)
    if (env.APP_BASE_URL && assetResponse.headers.get('content-type')?.includes('text/html')) {
      return new HTMLRewriter()
        .on('head', {
          element(element) {
            element.append(
              `<script>window.__APP_BASE_URL__=${JSON.stringify(env.APP_BASE_URL)}</script>`,
              { html: true },
            )
          },
        })
        .transform(assetResponse)
    }
    return assetResponse
  },
}
