import { useStepLabels } from '../../../hooks/useStepLabels'
import { REQUIRED_STEP_COLORS } from '../../../constants/deal'

interface RequiredStepTagsProps {
  requiredSteps?: string[]
}

/** Small tags for the screenshot-requiring steps of a deal's campaign (e.g. Rating, Review). */
export function RequiredStepTags({ requiredSteps }: RequiredStepTagsProps) {
  const labels = useStepLabels()

  return (
    <>
      {(requiredSteps ?? [])
        .filter(type => labels[type])
        .map(type => (
          <span
            key={type}
            className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', REQUIRED_STEP_COLORS[type]].join(' ')}
          >
            {labels[type]}
          </span>
        ))}
    </>
  )
}
