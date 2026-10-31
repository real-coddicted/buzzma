export function Footer() {
  return (
    <footer className="fixed bottom-0 left-0 right-0 z-20 flex items-center justify-center h-6 text-[11px] text-ink-light-muted dark:text-ink-dark-muted bg-surface-light-base/80 dark:bg-surface-dark-base/80 backdrop-blur-sm border-t border-surface-light-border dark:border-surface-dark-border">
      Powered By{' '}
      <a
        href="https://coddicted.com/"
        target="_blank"
        rel="noopener noreferrer"
        className="ml-1 text-neon-green hover:text-neon-cyan transition-colors"
      >
        Coddicted
      </a>
    </footer>
  )
}
