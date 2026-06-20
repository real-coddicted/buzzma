export interface LoginForm {
  mobile: string
  password: string
}

export interface LoginResponse {
  success: boolean
  message: string
  captchaFailed?: boolean
}
