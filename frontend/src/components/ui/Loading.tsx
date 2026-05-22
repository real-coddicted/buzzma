import type { SVGProps } from 'react'

interface LoadingProps extends Omit<SVGProps<SVGSVGElement>, 'width' | 'height'> {
  /** Diameter of the spinner in pixels. */
  size?: number
  /** Stroke thickness of the ring. */
  strokeWidth?: number
}

/** The eight neon accent colors from tailwind.config.ts, in spectrum order. */
const NEON_CYCLE = [
  '#ff5c57', // red
  '#ff79c6', // pink
  '#ffb86c', // orange
  '#f1fa8c', // yellow
  '#50fa7b', // green
  '#8be9fd', // cyan
  '#57c7ff', // blue
  '#bd93f9', // purple
  '#ff5c57', // back to red for a seamless loop
].join(';')

/**
 * Loading spinner — a ring that rotates while its arc continuously fades
 * through the eight neon accent colors. Rotation uses Tailwind's built-in
 * `animate-spin`; the color cycle is a self-contained SVG `<animate>` so it
 * works regardless of Tailwind config.
 */
export function Loading({ size = 24, strokeWidth = 3, className = '', ...props }: LoadingProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      role="status"
      aria-label="Loading"
      className={['animate-spin', className].join(' ')}
      {...props}
    >
      {/* faint static track */}
      <circle
        cx="12"
        cy="12"
        r="9"
        stroke="currentColor"
        strokeWidth={strokeWidth}
        className="text-surface-light-border dark:text-surface-dark-border"
      />
      {/* color-cycling arc */}
      <circle
        cx="12"
        cy="12"
        r="9"
        stroke="#ff5c57"
        strokeWidth={strokeWidth}
        strokeLinecap="round"
        strokeDasharray="56.5"
        strokeDashoffset="42"
      >
        <animate
          attributeName="stroke"
          values={NEON_CYCLE}
          dur="6s"
          calcMode="linear"
          repeatCount="indefinite"
        />
      </circle>
    </svg>
  )
}