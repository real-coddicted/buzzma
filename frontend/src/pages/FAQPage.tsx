import { useState, type ReactNode } from 'react'
import { PROMOTION_CATEGORY_PLATFORMS, PROMOTION_CATEGORY_STEPS, PLATFORM_LABELS } from '../constants/campaigns'
import { useStepLabels } from '../hooks/useStepLabels'
import { ComingSoonPlaceholder } from '../components/ui/ComingSoonPlaceholder'
import { IconChevronRight, IconShoppingBag, IconShoppingBagFast } from '../components/ui/icons'
import type { PromotionCategory, Platform } from '../types'

// FAQ currently covers only the shopping categories — the ones with an order → delivery → rating →
// review flow. Social/app-store platforms aren't covered yet (see issue #857 discussion).
const SHOPPING_CATEGORIES: PromotionCategory[] = ['ECOMMERCE', 'QUICK_COMMERCE']

const CATEGORY_ICONS: Record<PromotionCategory, typeof IconShoppingBag> = {
  ECOMMERCE: IconShoppingBag,
  QUICK_COMMERCE: IconShoppingBagFast,
  APP_PROMOTION: IconShoppingBag,
  SOCIAL_PAGE_PROMOTION: IconShoppingBag,
}

const CATEGORY_LABELS: Record<PromotionCategory, string> = {
  ECOMMERCE: 'E-commerce',
  QUICK_COMMERCE: 'Quick Commerce',
  APP_PROMOTION: 'App Promotion',
  SOCIAL_PAGE_PROMOTION: 'Social Page Promotion',
}

const POLICY_ITEMS = ['Order Submission', 'Refund Form Deadlines', 'Late Submission Penalties']

/** Looks up `/faq/<platform>/<step>.png` in public/ and falls back to the "coming soon" placeholder if it 404s. */
function ScreenshotSlot({ platform, step }: { platform: Platform; step: string }) {
  const [failed, setFailed] = useState(false)
  if (failed) return <ComingSoonPlaceholder variant="image" />
  return (
    <div className="w-full aspect-video max-w-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border overflow-hidden bg-white">
      <img
        src={`/faq/${platform}/${step}.png`}
        alt={`${PLATFORM_LABELS[platform]} — ${step} example`}
        className="w-full h-full object-contain"
        onError={() => setFailed(true)}
      />
    </div>
  )
}

function AccordionItem({ title, icon, children, defaultOpen = false }: { title: ReactNode; icon?: ReactNode; children: ReactNode; defaultOpen?: boolean }) {
  const [open, setOpen] = useState(defaultOpen)
  return (
    <div className="border border-surface-light-border dark:border-surface-dark-border rounded-lg overflow-hidden">
      <button
        type="button"
        onClick={() => setOpen(o => !o)}
        className="w-full flex items-center gap-3 px-4 py-3 text-left bg-surface-light-card dark:bg-surface-dark-card hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
      >
        {icon}
        <span className="flex-1 text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">{title}</span>
        <IconChevronRight size={14} className={`text-ink-light-muted dark:text-ink-dark-muted transition-transform ${open ? 'rotate-90' : ''}`} />
      </button>
      {open && (
        <div className="px-4 py-3 border-t border-surface-light-border dark:border-surface-dark-border bg-surface-light-base dark:bg-surface-dark-base space-y-3">
          {children}
        </div>
      )}
    </div>
  )
}

export function FAQPage() {
  const stepLabels = useStepLabels()

  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Frequently Asked Questions
        </h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          How to submit proof screenshots for your campaigns, by platform and screenshot type.
        </p>
        <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted mt-2">
          Note: screenshots should be taken from a web browser (desktop) unless otherwise specified. Screenshots from a native app may be rejected if they do not clearly show the required information.
        </p>
      </div>

      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">Screenshot Guide by Platform</h2>
        <div className="space-y-3">
          {SHOPPING_CATEGORIES.map(category => {
            const CategoryIcon = CATEGORY_ICONS[category]
            return (
            <AccordionItem
              key={category}
              title={CATEGORY_LABELS[category]}
              icon={<CategoryIcon size={16} />}
            >
              <div className="space-y-2">
                {PROMOTION_CATEGORY_PLATFORMS[category].map((platform: Platform) => (
                  <AccordionItem key={platform} title={PLATFORM_LABELS[platform]}>
                    <div className="space-y-4">
                      {PROMOTION_CATEGORY_STEPS[category].map(step => (
                        <div key={step}>
                          <p className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary mb-1">
                            {stepLabels[step] ?? step}
                          </p>
                          <ComingSoonPlaceholder variant="text" />
                          <div className="mt-2">
                            <ScreenshotSlot platform={platform} step={step} />
                          </div>
                        </div>
                      ))}
                    </div>
                  </AccordionItem>
                ))}
              </div>
            </AccordionItem>
          )})}
        </div>
      </section>

      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">Policies &amp; Guidelines</h2>
        <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5 space-y-4">
          {POLICY_ITEMS.map(item => (
            <div key={item}>
              <p className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary mb-1">{item}</p>
              <ComingSoonPlaceholder variant="text" />
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}
