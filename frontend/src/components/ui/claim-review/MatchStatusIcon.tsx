import { IconCheck, IconX, IconInfo } from '../icons'

interface Props {
  matched: boolean
  indeterminate?: boolean
}

export function MatchStatusIcon({ matched, indeterminate }: Props) {
  if (matched) {
    return (
      <span className="flex-shrink-0 inline-flex items-center justify-center w-4 h-4 rounded-full bg-neon-green/15 text-neon-green">
        <IconCheck size={9} />
      </span>
    )
  }
  if (indeterminate) {
    return (
      <span className="flex-shrink-0 inline-flex items-center justify-center w-4 h-4 rounded-full bg-neon-yellow/10 text-neon-yellow">
        <IconInfo size={9} />
      </span>
    )
  }
  return (
    <span className="flex-shrink-0 inline-flex items-center justify-center w-4 h-4 rounded-full bg-neon-red/15 text-neon-red">
      <IconX size={9} />
    </span>
  )
}
