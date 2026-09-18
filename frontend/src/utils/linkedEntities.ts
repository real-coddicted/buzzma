import type { LinkedEntity } from '../types'

export function copyFieldToAllRows(
  rows: { id: string; name: string }[],
  entityMap: Map<string, LinkedEntity>,
  field: 'slotsAvailable' | 'commissionOffered',
): LinkedEntity[] {
  const value = entityMap.get(rows[0]?.id ?? '')?.[field] ?? 0
  return rows.map(r => ({
    ...(entityMap.get(r.id) ?? { id: r.id, name: r.name, slotsAvailable: 0, commissionOffered: 0 }),
    [field]: value,
  }))
}
