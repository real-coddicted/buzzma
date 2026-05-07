import { useState } from 'react'
import { ImageFiller } from '../ImageFiller'

interface ProductThumbnailProps {
  src: string
  alt: string
  className?: string
  imgClassName?: string
}

export function ProductThumbnail({ src, alt, className = '', imgClassName = '' }: ProductThumbnailProps) {
  const [imgError, setImgError] = useState(false)

  return (
    <div className={['overflow-hidden bg-surface-light-hover dark:bg-surface-dark-hover', className].join(' ')}>
      {imgError ? (
        <ImageFiller />
      ) : (
        <img
          src={src}
          alt={alt}
          onError={() => setImgError(true)}
          className={['w-full h-full object-cover', imgClassName].join(' ')}
        />
      )}
    </div>
  )
}
