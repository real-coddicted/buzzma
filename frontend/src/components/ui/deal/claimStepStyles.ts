export const inputClass = [
  'w-full text-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border',
  'bg-surface-light-hover dark:bg-surface-dark-hover',
  'text-ink-light-primary dark:text-ink-dark-primary',
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted',
  'px-3 py-2 outline-none focus:border-neon-blue/50 transition-colors',
].join(' ')

export const labelClass = 'block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5'

export function submitBtnClass(color: string) {
  return `w-full py-2.5 rounded-lg text-surface-dark-base text-sm font-semibold transition-all disabled:opacity-40 disabled:cursor-not-allowed ${color}`
}
