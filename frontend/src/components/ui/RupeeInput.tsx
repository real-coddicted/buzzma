interface Props {
  value: string | number
  onChange: (value: string) => void
  placeholder?: string
  className?: string
  symbolOffset?: string
  inputPadding?: string
  disabled?: boolean
}

const noSpinner = '[appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none'

export function RupeeInput({
  value,
  onChange,
  placeholder = '0.00',
  className = '',
  symbolOffset = 'left-3',
  inputPadding = 'pl-6',
  disabled,
}: Props) {
  return (
    <div className="relative">
      <span className={`absolute ${symbolOffset} top-1/2 -translate-y-1/2 text-xs text-ink-light-muted dark:text-ink-dark-muted`}>₹</span>
      <input
        type="number"
        min="0"
        placeholder={placeholder}
        value={value}
        onChange={e => onChange(e.target.value)}
        disabled={disabled}
        className={[inputPadding, noSpinner, className].join(' ')}
      />
    </div>
  )
}