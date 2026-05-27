import { type MouseEvent } from 'react'

// Lens covers 35% of the image — magnified pane shows 1/0.35 ≈ 2.86× zoom.
export const LENS_FRACTION = 0.35

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
  pos,
  onHoverChange,
  onPosChange,
}: ClaimProofImageLensProps) {
  function handleMove(e: MouseEvent<HTMLDivElement>) {
    const rect = e.currentTarget.getBoundingClientRect()
    const x = clamp(((e.clientX - rect.left) / rect.width) * 100, 0, 100)
    const y = clamp(((e.clientY - rect.top) / rect.height) * 100, 0, 100)
    onPosChange({ x, y })
  }

  const lensPct = LENS_FRACTION * 100
  const lensX = clamp(pos.x - lensPct / 2, 0, 100 - lensPct)
  const lensY = clamp(pos.y - lensPct / 2, 0, 100 - lensPct)

  return (
    <div className="p-4 md:flex-1 md:min-w-0 md:max-w-[50%]">
      <div
        className="relative w-full aspect-square rounded-xl overflow-hidden bg-surface-light-hover dark:bg-surface-dark-hover cursor-crosshair select-none"
        onMouseEnter={() => onHoverChange(true)}
        onMouseLeave={() => onHoverChange(false)}
        onMouseMove={handleMove}
      >
        <img
          src={imageUrl}
          alt={imageAlt ?? ''}
          className="w-full h-full object-contain pointer-events-none"
          draggable={false}
        />
        {hovering && (
          <div
            className="absolute pointer-events-none border-2 border-neon-blue/70 bg-neon-blue/10"
            style={{
              left: `${lensX}%`,
              top: `${lensY}%`,
              width: `${lensPct}%`,
              height: `${lensPct}%`,
            }}
          />
        )}
      </div>
    </div>
  )
}

function clamp(n: number, min: number, max: number) {
  return Math.max(min, Math.min(max, n))
}
