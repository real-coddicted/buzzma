import type { components } from '../types/api'
import { fetchWithAuth } from './client'
import type { Notification } from '../types/NotificationTypes'
import type { NotificationTab } from '../components/ui/notifications/NotificationTabs'
import { toRelativeTime } from '../utils/time'

type NotificationResponseDto = components['schemas']['NotificationResponseDto']
type PagedNotificationsResponseDto = components['schemas']['PagedNotificationsResponseDto']

const API_BASE = '/api/v1'

export const NOTIFICATIONS_PAGE_SIZE = 10

export interface NotificationsPage {
  items: Notification[]
  total: number
  totalPages: number
}

function toNotification(dto: NotificationResponseDto): Notification {
  return {
    id: dto.id ?? '',
    title: dto.title ?? '',
    message: dto.message ?? '',
    time: dto.createdAt ? toRelativeTime(dto.createdAt) : '',
    unread: dto.status === 'NOTIFICATION_STATUS_UNREAD',
    pinned: dto.pinned ?? false,
    accent: 'text-ink-light-primary dark:text-ink-dark-primary',
  }
}

const STATUS_BY_TAB: Record<NotificationTab, string> = {
  unread: 'NOTIFICATION_STATUS_UNREAD',
  read: 'NOTIFICATION_STATUS_READ',
}

export async function fetchNotificationsPage(
  tab: NotificationTab,
  page: number,
  size = NOTIFICATIONS_PAGE_SIZE,
): Promise<NotificationsPage> {
  // Backend pagination is zero-based; the UI uses 1-based page numbers.
  const params = new URLSearchParams({
    status: STATUS_BY_TAB[tab],
    page: String(page - 1),
    size: String(size),
  })
  const res = await fetchWithAuth(`${API_BASE}/notifications?${params.toString()}`)
  const data = await res.json() as PagedNotificationsResponseDto
  const items = (data.items ?? []).map(toNotification)
  const total = data.total ?? items.length
  const totalPages = data.totalPages ?? Math.max(1, Math.ceil(total / size))
  return { items, total, totalPages }
}

export async function fetchUnreadNotificationCount(): Promise<number> {
  const res = await fetchWithAuth(`${API_BASE}/notifications/unread-count`)
  const data = await res.json() as { unread?: number }
  return data.unread ?? 0
}

export async function markAsRead(id: string): Promise<void> {
  await fetchWithAuth(`${API_BASE}/notifications/${id}/read`, { method: 'PUT' })
}

export async function markAsUnread(id: string): Promise<void> {
  await fetchWithAuth(`${API_BASE}/notifications/${id}/unread`, { method: 'PUT' })
}

export async function pinNotification(id: string): Promise<void> {
  await fetchWithAuth(`${API_BASE}/notifications/${id}/pin`, { method: 'PUT' })
}

export async function markAllRead(): Promise<void> {
  await fetchWithAuth(`${API_BASE}/notifications/read-all`, { method: 'PUT' })
}