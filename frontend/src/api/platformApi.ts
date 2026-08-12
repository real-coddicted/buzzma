import type { PlatformOption } from '../types/PlatformTypes'
import { PLATFORM_LABELS } from '../constants/campaigns'

export async function fetchPlatforms(): Promise<PlatformOption[]> {
  await new Promise(resolve => setTimeout(resolve, 300))
  return Object.entries(PLATFORM_LABELS).map(([value, label]) => ({ value, label })) as PlatformOption[]
}
