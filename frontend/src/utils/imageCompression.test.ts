import { describe, it, expect, vi, afterEach } from 'vitest'
import { compressImage, renameForType, scaledDimensions, SKIP_BELOW_BYTES } from './imageCompression'

describe('scaledDimensions', () => {
  it('caps the short side of a tall screenshot and keeps the aspect ratio', () => {
    expect(scaledDimensions(1440, 3200)).toEqual({ width: 1080, height: 2400 })
  })

  it('caps the short side of a landscape image', () => {
    expect(scaledDimensions(3200, 1440)).toEqual({ width: 2400, height: 1080 })
  })

  it('never upscales an image already within the limit', () => {
    expect(scaledDimensions(720, 1600)).toEqual({ width: 720, height: 1600 })
  })
})

describe('renameForType', () => {
  it('replaces the extension with .webp', () => {
    expect(renameForType('Screenshot 2026.10.09.png', 'image/webp')).toBe('Screenshot 2026.10.09.webp')
  })

  it('replaces the extension with .jpg for jpeg', () => {
    expect(renameForType('order.png', 'image/jpeg')).toBe('order.jpg')
  })

  it('appends an extension when the name has none', () => {
    expect(renameForType('order', 'image/webp')).toBe('order.webp')
  })
})

/** File of `size` bytes; compressImage only inspects name/type/size before decoding. */
function fakeFile(size: number, type = 'image/png', name = 'shot.png'): File {
  return new File([new Uint8Array(size)], name, { type })
}

/** Stubs bitmap decoding and a canvas whose toBlob yields `encoded` per requested type. */
function stubCanvas(encoded: Record<string, Blob | null>) {
  vi.stubGlobal('createImageBitmap', vi.fn(async () => ({ width: 1440, height: 3200, close: vi.fn() })))
  const canvas = {
    width: 0,
    height: 0,
    getContext: () => ({ drawImage: vi.fn() }),
    toBlob: (cb: (b: Blob | null) => void, type: string) => cb(encoded[type] ?? null),
  }
  vi.stubGlobal('document', { createElement: () => canvas })
  return canvas
}

describe('compressImage', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('returns small files untouched without decoding', async () => {
    const decode = vi.fn()
    vi.stubGlobal('createImageBitmap', decode)
    const file = fakeFile(SKIP_BELOW_BYTES)
    expect(await compressImage(file)).toBe(file)
    expect(decode).not.toHaveBeenCalled()
  })

  it('returns non-image files untouched', async () => {
    const file = fakeFile(1_000_000, 'application/pdf', 'doc.pdf')
    expect(await compressImage(file)).toBe(file)
  })

  it('downscales and re-encodes as webp', async () => {
    const canvas = stubCanvas({ 'image/webp': new Blob([new Uint8Array(200_000)], { type: 'image/webp' }) })
    const result = await compressImage(fakeFile(2_000_000))
    expect(result.type).toBe('image/webp')
    expect(result.name).toBe('shot.webp')
    expect(result.size).toBe(200_000)
    expect(canvas.width).toBe(1080)
    expect(canvas.height).toBe(2400)
  })

  it('falls back to jpeg when the browser cannot encode webp', async () => {
    stubCanvas({
      'image/webp': new Blob([new Uint8Array(900_000)], { type: 'image/png' }),
      'image/jpeg': new Blob([new Uint8Array(250_000)], { type: 'image/jpeg' }),
    })
    const result = await compressImage(fakeFile(2_000_000))
    expect(result.type).toBe('image/jpeg')
    expect(result.name).toBe('shot.jpg')
  })

  it('returns the original when the compressed result is not smaller', async () => {
    stubCanvas({ 'image/webp': new Blob([new Uint8Array(600_000)], { type: 'image/webp' }) })
    const file = fakeFile(500_000)
    expect(await compressImage(file)).toBe(file)
  })

  it('returns the original when the image cannot be decoded', async () => {
    vi.stubGlobal('createImageBitmap', vi.fn(async () => { throw new Error('unsupported') }))
    const file = fakeFile(2_000_000, 'image/heic', 'shot.heic')
    expect(await compressImage(file)).toBe(file)
  })
})
