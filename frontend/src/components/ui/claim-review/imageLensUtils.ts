import type { MouseEvent, SyntheticEvent } from 'react'

// Lens covers 35% of the image — magnified pane shows 1/0.35 ≈ 2.86× zoom.
export const LENS_FRACTION = 0.35

export interface ImageBounds {
  left: number   // fraction of container width [0,1]
  top: number    // fraction of container height [0,1]
  width: number  // fraction of container width [0,1]
  height: number // fraction of container height [0,1]
}

export const FULL_BOUNDS: ImageBounds = { left: 0, top: 0, width: 1, height: 1 }

export function clamp(n: number, min: number, max: number) {
  return Math.max(min, Math.min(max, n))
}

export function computeImageBounds(
  e: SyntheticEvent<HTMLImageElement>,
  container: HTMLDivElement,
): ImageBounds | null {
  const img = e.currentTarget
  if (!img.naturalWidth || !img.naturalHeight) return null

  const cRect = container.getBoundingClientRect()
  const iRect = img.getBoundingClientRect()

  const imageAspect = img.naturalWidth / img.naturalHeight
  const elemAspect = iRect.width / iRect.height
  let rw: number, rh: number
  if (imageAspect > elemAspect) {
    rw = iRect.width; rh = iRect.width / imageAspect
  } else {
    rh = iRect.height; rw = iRect.height * imageAspect
  }

  const cw = cRect.width, ch = cRect.height
  return {
    left: (iRect.left - cRect.left + (iRect.width - rw) / 2) / cw,
    top: (iRect.top - cRect.top + (iRect.height - rh) / 2) / ch,
    width: rw / cw,
    height: rh / ch,
  }
}

export function computeLensAndPos(
  e: MouseEvent<HTMLDivElement>,
  imgBounds: ImageBounds,
): { lensCenter: { x: number; y: number }; pos: { x: number; y: number } } {
  const rect = e.currentTarget.getBoundingClientRect()
  const rawX = ((e.clientX - rect.left) / rect.width) * 100
  const rawY = ((e.clientY - rect.top) / rect.height) * 100

  const il = imgBounds.left * 100
  const it = imgBounds.top * 100
  const iw = imgBounds.width * 100
  const ih = imgBounds.height * 100

  const cx = clamp(rawX, il, il + iw)
  const cy = clamp(rawY, it, it + ih)

  const lensW = LENS_FRACTION * iw
  const lensH = LENS_FRACTION * ih
  const lx = clamp(cx - lensW / 2, il, il + iw - lensW)
  const ly = clamp(cy - lensH / 2, it, it + ih - lensH)

  return {
    lensCenter: { x: cx, y: cy },
    pos: {
      x: clamp(((lx - il) / iw) / (1 - LENS_FRACTION) * 100, 0, 100),
      y: clamp(((ly - it) / ih) / (1 - LENS_FRACTION) * 100, 0, 100),
    },
  }
}
