export function paiseToRupees(paise: number): number {
  return paise / 100
}

export function rupeesToPaise(rupees: number): number {
  const sign = rupees < 0 ? -1 : 1
  const [whole, decimal = ''] = Math.abs(rupees).toFixed(2).split('.')
  return sign * (parseInt(whole, 10) * 100 + parseInt(decimal, 10))
}

export function formatRupees(amount: number): string {
  return amount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
