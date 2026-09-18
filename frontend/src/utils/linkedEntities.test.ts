import { describe, it, expect } from 'vitest'
import { copyFieldToAllRows } from './linkedEntities'
import type { LinkedEntity } from '../types'

describe('copyFieldToAllRows', () => {
  it('copies slotsAvailable to all rows when only the first row is selected', () => {
    const rows = [
      { id: '1', name: 'Mediator 1' },
      { id: '2', name: 'Mediator 2' },
      { id: '3', name: 'Mediator 3' },
    ]
    const entityMap = new Map<string, LinkedEntity>([
      ['1', { id: '1', name: 'Mediator 1', slotsAvailable: 5, commissionOffered: 0 }],
    ])

    const result = copyFieldToAllRows(rows, entityMap, 'slotsAvailable')

    expect(result).toEqual([
      { id: '1', name: 'Mediator 1', slotsAvailable: 5, commissionOffered: 0 },
      { id: '2', name: 'Mediator 2', slotsAvailable: 5, commissionOffered: 0 },
      { id: '3', name: 'Mediator 3', slotsAvailable: 5, commissionOffered: 0 },
    ])
  })

  it('copies commissionOffered to all rows', () => {
    const rows = [
      { id: '1', name: 'Mediator 1' },
      { id: '2', name: 'Mediator 2' },
    ]
    const entityMap = new Map<string, LinkedEntity>([
      ['1', { id: '1', name: 'Mediator 1', slotsAvailable: 0, commissionOffered: 100 }],
      ['2', { id: '2', name: 'Mediator 2', slotsAvailable: 0, commissionOffered: 50 }],
    ])

    const result = copyFieldToAllRows(rows, entityMap, 'commissionOffered')

    expect(result).toEqual([
      { id: '1', name: 'Mediator 1', slotsAvailable: 0, commissionOffered: 100 },
      { id: '2', name: 'Mediator 2', slotsAvailable: 0, commissionOffered: 100 },
    ])
  })

  it('handles an empty rows array', () => {
    const result = copyFieldToAllRows([], new Map(), 'slotsAvailable')
    expect(result).toEqual([])
  })

  it('defaults to 0 when the first row has no entity yet', () => {
    const rows = [
      { id: '1', name: 'Mediator 1' },
      { id: '2', name: 'Mediator 2' },
    ]

    const result = copyFieldToAllRows(rows, new Map(), 'slotsAvailable')

    expect(result).toEqual([
      { id: '1', name: 'Mediator 1', slotsAvailable: 0, commissionOffered: 0 },
      { id: '2', name: 'Mediator 2', slotsAvailable: 0, commissionOffered: 0 },
    ])
  })
})
