import type { Theme } from '../../types'
import { IconSun, IconMoon } from './icons'

interface ThemeToggleProps {
  theme: Theme
  onToggle: () => void
}

export function ThemeToggle({ theme, onToggle }: ThemeToggleProps) {
  return (
    <button
      onClick={onToggle}
      title={`Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`}
      className="relative w-14 h-7 rounded-full transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-neon-blue/50"
      style={{
        background: theme === 'dark'
          ? 'linear-gradient(135deg, #1e1e1e 0%, #2a2a2a 100%)'
          : 'linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%)',
        border: theme === 'dark' ? '1px solid #3a3a3a' : '1px solid #93c5fd',
      }}
    >
      <span
        className="absolute top-0.5 w-6 h-6 rounded-full flex items-center justify-center transition-all duration-300 shadow-md"
        style={{
          left: theme === 'dark' ? 'calc(100% - 26px)' : '2px',
          background: theme === 'dark'
            ? 'linear-gradient(135deg, #bd93f9 0%, #57c7ff 100%)'
            : 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)',
        }}
      >
        {theme === 'dark' ? (
          <IconMoon size={12} className="text-white" />
        ) : (
          <IconSun size={12} className="text-white" />
        )}
      </span>
    </button>
  )
}
