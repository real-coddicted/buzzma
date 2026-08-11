interface ClaimsTableProps {
  children: React.ReactNode
}

export function ClaimsTable({ children }: ClaimsTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-surface-light-border dark:border-surface-dark-border">
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Order ID</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Campaign</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Brand</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Approved</th>
            <th className="px-4 py-2 text-right text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Amount</th>
          </tr>
        </thead>
        <tbody>{children}</tbody>
      </table>
    </div>
  )
}
