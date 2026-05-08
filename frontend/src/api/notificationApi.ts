import type { Notification } from '../types/NotificationTypes'
import { notifications as mockNotifications } from '../data/mockData'

export async function fetchNotifications(): Promise<Notification[]> {
  await new Promise(resolve => setTimeout(resolve, 400))
  return mockNotifications
}
