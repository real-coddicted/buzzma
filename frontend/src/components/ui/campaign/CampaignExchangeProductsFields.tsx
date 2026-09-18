import { useEffect, useState } from 'react'
import type { CampaignType } from '../../../types'
import { errorClass, isValidImageUrl, mergeExchangeProductRows, type ExchangeProductRow } from './campaignFormConstants'
import { ProductThumbnail } from './ProductThumbnail'
import { fetchExchangeProducts, createExchangeProduct } from '../../../api/exchangeProductApi'
import { Button } from '../Button'

interface FormSlice {
  campaignType: CampaignType | ''
  exchangeProducts: ExchangeProductRow[]
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: 'exchangeProducts', value: unknown) => void
  readOnly?: boolean
}

const cellInputClass = [
  'w-full bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg px-2 py-1',
  'text-xs text-ink-light-primary dark:text-ink-dark-primary outline-none',
  'focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all disabled:opacity-40',
].join(' ')

export function CampaignExchangeProductsFields({ form, errors, set, readOnly }: Props) {
  const isExchange = form.campaignType === 'CAMPAIGN_TYPE_EXCHANGE'

  const [loadError, setLoadError] = useState<string | null>(null)
  const [newName, setNewName] = useState('')
  const [newImageUrl, setNewImageUrl] = useState('')
  const [adding, setAdding] = useState(false)
  const [addError, setAddError] = useState<string | null>(null)

  // Re-fetch the agency master list every time the section becomes visible.
  useEffect(() => {
    if (!isExchange || readOnly) return
    let cancelled = false
    setLoadError(null)
    fetchExchangeProducts()
      .then(master => {
        if (cancelled) return
        set('exchangeProducts', mergeExchangeProductRows(master, form.exchangeProducts))
      })
      .catch(() => {
        if (!cancelled) setLoadError('Could not load saved exchange products. You can still add products below.')
      })
    return () => { cancelled = true }
    // form.exchangeProducts is intentionally excluded: merge runs against whatever rows exist when the fetch resolves.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isExchange, readOnly])

  if (!isExchange) return null

  const rows = form.exchangeProducts

  function updateRow(idx: number, patch: Partial<ExchangeProductRow>) {
    set('exchangeProducts', rows.map((r, i) => (i === idx ? { ...r, ...patch } : r)))
  }

  async function handleAdd() {
    const name = newName.trim()
    if (!name) return
    setAdding(true)
    setAddError(null)
    try {
      await createExchangeProduct(name)
      set('exchangeProducts', [
        ...rows,
        { productName: name, productImageUrl: newImageUrl.trim(), selected: true, prefilled: true },
      ])
      setNewName('')
      setNewImageUrl('')
    } catch (err) {
      setAddError(err instanceof Error ? err.message : 'Could not add this product.')
    } finally {
      setAdding(false)
    }
  }

  if (readOnly) {
    const selected = rows.filter(r => r.selected && r.productName.trim())
    return (
      <section className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-5 space-y-4">
        <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-pink">Exchange Products</h3>
        {selected.length === 0 ? (
          <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">No exchange products selected.</p>
        ) : (
          <ul className="space-y-2">
            {selected.map((r, i) => (
              <li key={i} className="flex items-center gap-3">
                <ProductThumbnail src={r.productImageUrl} alt={r.productName} />
                <span className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary">{r.productName}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    )
  }

  return (
    <section className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-5 space-y-4">
      <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-pink">Exchange Products</h3>
      <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
        Select the products a buyer can exchange under this campaign. Add new products to your agency list with the row below.
      </p>

      {loadError && <p className={errorClass}>{loadError}</p>}

      <div className="overflow-auto max-h-[420px] rounded-lg border border-surface-light-border dark:border-surface-dark-border">
        <table className="w-full text-xs">
          <thead>
            <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
              <th className="px-4 py-2.5 w-10" />
              <th className="text-left px-4 py-2.5 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Product Name *</th>
              <th className="text-left px-4 py-2.5 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Image URL (Optional)</th>
              <th className="px-4 py-2.5 w-14" />
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {rows.map((row, idx) => {
              const urlInvalid = !isValidImageUrl(row.productImageUrl)
              return (
                <tr key={idx} className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors align-top">
                  <td className="px-4 py-3">
                    <input
                      type="checkbox"
                      checked={row.selected}
                      onChange={e => updateRow(idx, { selected: e.target.checked })}
                      className="accent-neon-blue cursor-pointer"
                    />
                  </td>
                  <td className="px-4 py-3">
                    <input
                      type="text"
                      value={row.productName}
                      readOnly={row.prefilled}
                      onChange={e => updateRow(idx, { productName: e.target.value })}
                      className={[cellInputClass, row.prefilled ? 'opacity-70 cursor-not-allowed' : ''].join(' ')}
                    />
                  </td>
                  <td className="px-4 py-3">
                    <input
                      type="url"
                      placeholder="e.g. https://example.com/image.jpg"
                      value={row.productImageUrl}
                      onChange={e => updateRow(idx, { productImageUrl: e.target.value })}
                      className={cellInputClass}
                    />
                    {urlInvalid && <p className={errorClass}>Enter a valid http(s) URL</p>}
                  </td>
                  <td className="px-4 py-3">
                    <ProductThumbnail src={row.productImageUrl} alt={row.productName || '?'} />
                  </td>
                </tr>
              )
            })}

            <tr className="align-top bg-surface-light-hover/40 dark:bg-surface-dark-hover/40">
              <td className="px-4 py-3" />
              <td className="px-4 py-3">
                <input
                  type="text"
                  placeholder="New product name"
                  value={newName}
                  onChange={e => setNewName(e.target.value)}
                  className={cellInputClass}
                />
              </td>
              <td className="px-4 py-3">
                <input
                  type="url"
                  placeholder="e.g. https://example.com/image.jpg"
                  value={newImageUrl}
                  onChange={e => setNewImageUrl(e.target.value)}
                  className={cellInputClass}
                />
                {addError && <p className={errorClass}>{addError}</p>}
              </td>
              <td className="px-4 py-3">
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  disabled={!newName.trim() || adding}
                  loading={adding}
                  onClick={handleAdd}
                >
                  Add
                </Button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      {errors.exchangeProducts && <p className={errorClass}>{errors.exchangeProducts}</p>}
    </section>
  )
}
