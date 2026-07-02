import type { LoginAs } from './RegisterTypes'

export interface UserDetails {
  code?: string
  type?: LoginAs | 'admin'
  name: string
  mobile: string
  email?: string
}

export interface UserBankingDto {
  bankAccountNumber?: string
  bankIfscCode?: string
  bankName?: string
  bankAccountHolderName?: string
}

export interface UserActivityDto {
  orderCount: number
  connectionCount: number
}