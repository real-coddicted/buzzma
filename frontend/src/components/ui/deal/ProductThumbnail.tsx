import { useEffect, useRef, useState } from 'react'
import { ImageFiller } from '../ImageFiller'
import { IconChevronLeft, IconChevronRight } from '../icons'

const AUTOPLAY_MS = 4000

interface ProductThumbnailProps {
  /** A single image URL, or a list. With 2+ entries the thumbnail becomes a carousel. */
  src: string | string[]
  alt: string
  className?: string
  imgClassName?: string
  /** Auto-advance through the images (only when there are 2+). Ping-pongs at the ends. */
  autoPlay?: boolean
  /** When set, every image after the first is tagged with this pill (e.g. exchange options vs. the primary product). */
  secondaryLabel?: string
}

function Slide({ url, alt, imgClassName, active, label }: { url: string; alt: string; imgClassName: string; active: boolean; label?: string }) {
  const [error, setError] = useState(false)
  return (
    <div className="relative w-full h-full shrink-0" aria-hidden={!active}>
      {error ? (
        <ImageFiller />
      ) : (
        <img
          src={url}
          alt={alt}
          onError={() => setError(true)}
          className={['w-full h-full object-contain', imgClassName].join(' ')}
        />
      )}
      {label && (
        <span className="absolute bottom-2 left-2 z-10 rounded-full bg-black/55 text-white backdrop-blur-sm text-[10px] font-semibold px-2 py-0.5">
          {label}
        </span>
      )}
    </div>
  )
}

function SingleImage({ url, alt, imgClassName }: { url: string; alt: string; imgClassName: string }) {
  const [error, setError] = useState(false)
  if (error) return <ImageFiller />
  return (
    <img
      src={url}
      alt={alt}
      onError={() => setError(true)}
      className={['w-full h-full object-contain', imgClassName].join(' ')}
    />
  )
}

export function ProductThumbnail({ src, alt, className = '', imgClassName = '', autoPlay = false, secondaryLabel }: ProductThumbnailProps) {
  const images = (Array.isArray(src) ? src : [src]).filter(Boolean)
  const count = images.length

  const [index, setIndex] = useState(0)
  const [paused, setPaused] = useState(false)
  const direction = useRef(1)
  const touchStartX = useRef<number | null>(null)

  const [reducedMotion, setReducedMotion] = useState(
    () => window.matchMedia('(prefers-reduced-motion: reduce)').matches,
  )
  useEffect(() => {
    const mq = window.matchMedia('(prefers-reduced-motion: reduce)')
    const onChange = () => setReducedMotion(mq.matches)
    mq.addEventListener('change', onChange)
    return () => mq.removeEventListener('change', onChange)
  }, [])

  useEffect(() => {
    if (!autoPlay || reducedMotion || paused || count < 2) return
    const id = setInterval(() => {
      setIndex(i => {
        let next = i + direction.current
        if (next > count - 1) { direction.current = -1; next = i - 1 }
        else if (next < 0) { direction.current = 1; next = i + 1 }
        return next
      })
    }, AUTOPLAY_MS)
    return () => clearInterval(id)
  }, [autoPlay, reducedMotion, paused, count])

  const outerClassName = ['overflow-hidden bg-surface-light-hover dark:bg-surface-dark-hover', className].join(' ')

  if (count === 0) {
    return (
      <div className={outerClassName}>
        <ImageFiller />
      </div>
    )
  }

  if (count === 1) {
    return (
      <div className={outerClassName}>
        <SingleImage url={images[0]} alt={alt} imgClassName={imgClassName} />
      </div>
    )
  }

  function go(next: number) {
    direction.current = next >= index ? 1 : -1
    setIndex(Math.max(0, Math.min(count - 1, next)))
  }

  function onKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'ArrowLeft' && index > 0) { e.preventDefault(); go(index - 1) }
    else if (e.key === 'ArrowRight' && index < count - 1) { e.preventDefault(); go(index + 1) }
  }

  function onTouchStart(e: React.TouchEvent) {
    touchStartX.current = e.touches[0].clientX
  }

  function onTouchEnd(e: React.TouchEvent) {
    if (touchStartX.current === null) return
    const dx = e.changedTouches[0].clientX - touchStartX.current
    touchStartX.current = null
    if (dx <= -40 && index < count - 1) go(index + 1)
    else if (dx >= 40 && index > 0) go(index - 1)
  }

  return (
    <div className={outerClassName}>
      <div
        className="relative w-full h-full"
        role="group"
        aria-roledescription="carousel"
        aria-label={alt}
        tabIndex={0}
        onKeyDown={onKeyDown}
        onMouseEnter={() => setPaused(true)}
        onMouseLeave={() => setPaused(false)}
        onFocus={() => setPaused(true)}
        onBlur={e => { if (!e.currentTarget.contains(e.relatedTarget)) setPaused(false) }}
        onTouchStart={onTouchStart}
        onTouchEnd={onTouchEnd}
      >
        <div
          className="flex w-full h-full"
          style={{
            transform: `translateX(-${index * 100}%)`,
            transition: reducedMotion ? 'none' : 'transform 300ms ease-out',
          }}
        >
          {images.map((url, i) => (
            <Slide key={i} url={url} alt={alt} imgClassName={imgClassName} active={i === index} label={i === 0 ? undefined : secondaryLabel} />
          ))}
        </div>

        {index > 0 && (
          <button
            type="button"
            aria-label="Previous image"
            onClick={e => { e.stopPropagation(); go(index - 1) }}
            className="absolute left-1.5 top-1/2 -translate-y-1/2 z-10 grid place-items-center w-7 h-7 rounded-full bg-black/40 text-white backdrop-blur-sm hover:bg-black/60 transition-colors"
          >
            <IconChevronLeft size={16} />
          </button>
        )}
        {index < count - 1 && (
          <button
            type="button"
            aria-label="Next image"
            onClick={e => { e.stopPropagation(); go(index + 1) }}
            className="absolute right-1.5 top-1/2 -translate-y-1/2 z-10 grid place-items-center w-7 h-7 rounded-full bg-black/40 text-white backdrop-blur-sm hover:bg-black/60 transition-colors"
          >
            <IconChevronRight size={16} />
          </button>
        )}

        <div className="absolute bottom-1.5 left-1/2 -translate-x-1/2 z-10 flex items-center gap-1">
          {images.map((_, i) => (
            <button
              key={i}
              type="button"
              aria-label={`Go to image ${i + 1}`}
              aria-current={i === index}
              onClick={e => { e.stopPropagation(); go(i) }}
              className={[
                'rounded-full transition-all',
                i === index ? 'w-3 h-1.5 bg-white' : 'w-1.5 h-1.5 bg-white/50 hover:bg-white/80',
              ].join(' ')}
            />
          ))}
        </div>
      </div>
    </div>
  )
}
