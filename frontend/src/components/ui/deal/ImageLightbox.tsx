import { useEffect } from 'react'
import { IconX } from '../icons'

interface ImageLightboxProps {
  src: string
  alt: string
  onClose: () => void
}

export function ImageLightbox({ src, alt, onClose }: ImageLightboxProps) {
  useEffect(() => {
    function onKey(e: KeyboardEvent) { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm"
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="relative max-w-[95vw] max-h-[90vh] flex flex-col rounded-xl overflow-hidden shadow-2xl">
        <button
          onClick={onClose}
          className="absolute top-2 right-2 z-10 inline-flex items-center justify-center w-7 h-7 rounded-lg bg-black/50 text-white hover:bg-black/70 transition-colors"
        >
          <IconX size={14} />
        </button>
        <img src={src} alt={alt} className="max-w-full max-h-[90vh] object-contain" />
      </div>
    </div>
  )
}
