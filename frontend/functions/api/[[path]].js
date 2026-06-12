const BACKEND_URL = 'https://test-backend.buzzmah.com'

export async function onRequest(context) {
  const url = new URL(context.request.url)
  const target = `${BACKEND_URL}${url.pathname}${url.search}`

  return fetch(target, {
    method: context.request.method,
    headers: context.request.headers,
    body: ['GET', 'HEAD'].includes(context.request.method) ? undefined : context.request.body,
    redirect: 'follow',
  })
}
