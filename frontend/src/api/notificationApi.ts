import type { components } from '../types/api'
import { fetchWithAuth } from './client'
import type { Notification } from '../types/NotificationTypes'
import { toRelativeTime } from '../utils/time'

type NotificationResponseDto = components['schemas']['NotificationResponseDto']

const API_BASE = '/api/v1'

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

export async function fetchNotifications(): Promise<Notification[]> {
  const res = await fetchWithAuth(`${API_BASE}/notifications`)
  const data = await res.json() as NotificationResponseDto[]
  return data.map(toNotification)
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