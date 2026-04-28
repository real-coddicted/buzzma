export type LoginAs = 'brand' | 'agency'

export interface RegisterForm {
  registerAs: LoginAs
  mobile: string
  password: string
  inviteCode: string
  brandName: string
  agencyName: string
  securityQuestion1: string
  securityAnswer1: string
  securityQuestion2: string
  securityAnswer2: string
}

export interface RegisterResponse {
  success: boolean
  message: string
}
