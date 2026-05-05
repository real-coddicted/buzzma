import type { PlatformOption } from '../types/PlatformTypes'
import { platforms } from '../data/mockData'

export async function fetchPlatforms(): Promise<PlatformOption[]> {
  await new Promise(resolve => setTimeout(resolve, 300))
  return platforms
}
