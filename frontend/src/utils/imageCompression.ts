/** Screenshots are tall, so cap the short side: keeps text legible for AI extraction. */
export const MAX_SHORT_SIDE = 1080
/** Files at or below this size are uploaded as-is. */
export const SKIP_BELOW_BYTES = 300 * 1024
/** Below ~0.8, artifacts on small text (order IDs, amounts) start hurting extraction. */
export const QUALITY = 0.85

/** Target dimensions with the short side capped at `maxShortSide`; never upscales. */
export function scaledDimensions(
  width: number,
  height: number,
  maxShortSide: number = MAX_SHORT_SIDE,
): { width: number; height: number } {
  const scale = Math.min(1, maxShortSide / Math.min(width, height))
  return { width: Math.round(width * scale), height: Math.round(height * scale) }
}

/** `name` with its extension replaced to match `mimeType` (image/webp -> .webp, image/jpeg -> .jpg). */
export function renameForType(name: string, mimeType: string): string {
  const ext = mimeType === 'image/webp' ? 'webp' : 'jpg'
  const dot = name.lastIndexOf('.')
  const base = dot > 0 ? name.slice(0, dot) : name
  return `${base}.${ext}`
}

function toBlob(canvas: HTMLCanvasElement, type: string): Promise<Blob | null> {
  return new Promise(resolve => canvas.toBlob(resolve, type, QUALITY))
}

/**
 * Downscales and re-encodes an image (WebP, falling back to JPEG where the browser
 * can't encode WebP). Returns the original file if it's already small, can't be
 * decoded (e.g. HEIC outside Safari), or wouldn't get any smaller.
 */
export async function compressImage(file: File): Promise<File> {
  if (!file.type.startsWith('image/') || file.size <= SKIP_BELOW_BYTES) return file

  try {
    const bitmap = await createImageBitmap(file)
    const { width, height } = scaledDimensions(bitmap.width, bitmap.height)
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height
    const ctx = canvas.getContext('2d')
    if (!ctx) {
      bitmap.close()
      return file
    }
    ctx.drawImage(bitmap, 0, 0, width, height)
    bitmap.close()

    // Browsers that can't encode WebP silently return PNG instead, so check the type.
    let blob = await toBlob(canvas, 'image/webp')
    if (!blob || blob.type !== 'image/webp') blob = await toBlob(canvas, 'image/jpeg')
    if (!blob || blob.size >= file.size) return file

    return new File([blob], renameForType(file.name, blob.type), {
      type: blob.type,
      lastModified: file.lastModified,
    })
  } catch {
    return file
  }
}
