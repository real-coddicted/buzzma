import type { AssignmentItem } from '../types/AssignmentTypes'
import { unpublishedAssignments } from '../data/mockData'

export async function fetchUnpublishedAssignments(): Promise<AssignmentItem[]> {
  await new Promise(resolve => setTimeout(resolve, 400))
  return unpublishedAssignments
}
