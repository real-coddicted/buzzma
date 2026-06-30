import { useState, useRef, type SyntheticEvent, type MouseEvent } from 'react'
import {
  LENS_FRACTION,
  FULL_BOUNDS,
  clamp,
  computeImageBounds,
  computeLensAndPos,
  type ImageBounds,
} from './imageLensUtils'

interface ClaimProofImageLensProps {
  imageUrl: string
  imageAlt?: string
  hovering: boolean
  pos: { x: number; y: number }
  onHoverChange: (hovering: boolean) => void
  onPosChange: (pos: { x: number; y: number }) => void
}

export function ClaimProofImageLens({
  imageUrl,
  imageAlt,
  hovering,
  onHoverChange,
  onPosChange,
}: ClaimProofImageLensProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const [imgBounds, setImgBounds] = useState<ImageBounds>(FULL_BOUNDS)
  const [lensCenter, setLensCenter] = useState({ x: 50, y: 50 })

  function handleImageLoad(e: SyntheticEvent<HTMLImageElement>) {
    const container = containerRef.current
    if (!container) return
    const bounds = computeImageBounds(e, container)
    if (bounds) setImgBounds(bounds)
  }

  function handleMove(e: MouseEvent<HTMLDivElement>) {
    const { lensCenter: lc, pos } = computeLensAndPos(e, imgBounds)
    setLensCenter(lc)
    onPosChange(pos)
  }

  const il = imgBounds.left * 100
  const it = imgBounds.top * 100
  const iw = imgBounds.width * 100
  const ih = imgBounds.height * 100
  const lensW = LENS_FRACTION * iw
  const lensH = LENS_FRACTION * ih
  const lensX = clamp(lensCenter.x - lensW / 2, il, il + iw - lensW)
  const lensY = clamp(lensCenter.y - lensH / 2, it, it + ih - lensH)

  return (
    <div className="p-4 md:flex-1 md:min-w-0 md:max-w-[50%]">
      <div
        ref={containerRef}
        className="relative w-full aspect-square rounded-xl overflow-hidden bg-surface-light-hover dark:bg-surface-dark-hover cursor-crosshair select-none px-[5%]"
        onMouseEnter={() => onHoverChange(true)}
        onMouseLeave={() => onHoverChange(false)}
        onMouseMove={handleMove}
      >
        <img
          src={imageUrl}
          alt={imageAlt ?? ''}
          className="w-full h-full object-contain pointer-events-none"
          draggable={false}
          onLoad={handleImageLoad}
        />
        {hovering && (
          <div
            className="absolute pointer-events-none border-2 border-neon-blue/70 bg-neon-blue/10"
            style={{
              left: `${lensX}%`,
              top: `${lensY}%`,
              width: `${lensW}%`,
              height: `${lensH}%`,
            }}
          />
        )}
      </div>
    </div>
  )
}
