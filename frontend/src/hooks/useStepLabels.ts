import { useEffect, useState } from 'react'
import { fetchStepConfig } from '../api/campaignApi'

/** Fetches the campaign-independent step type → label map once (cached by fetchStepConfig itself). */
export function useStepLabels(): Record<string, string> {
  const [labels, setLabels] = useState<Record<string, string>>({})
  useEffect(() => {
    fetchStepConfig()
      .then(cfg => setLabels(Object.fromEntries(cfg.map(s => [s.type, s.label]))))
      .catch(() => {})
  }, [])
  return labels
}
