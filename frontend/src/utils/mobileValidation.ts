export const MOBILE_REGEX = /^[0-9]{10}$/

export function isValidMobile(mobile: string): boolean {
  return MOBILE_REGEX.test(mobile.replace(/\s/g, ''))
}
