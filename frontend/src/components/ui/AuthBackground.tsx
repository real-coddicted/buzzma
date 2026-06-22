import { useMemo } from 'react'

const NEONS = ['--neon-blue','--neon-green','--neon-purple','--neon-pink','--neon-cyan','--neon-orange','--neon-yellow','--neon-red']

const glow = (c: string) =>
  `drop-shadow(0 0 4px rgb(var(${c}))) drop-shadow(0 0 10px rgb(var(${c}) / 0.53))`

const pick = <T,>(arr: T[]) => arr[Math.floor(Math.random() * arr.length)]
const rnd  = (a: number, b: number) => a + Math.random() * (b - a)

const ICONS = [
  // shopping bag
  <><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 01-8 0"/></>,
  // headphones
  <><path d="M3 18v-6a9 9 0 0118 0v6"/><rect x="2" y="14" width="4" height="7" rx="2"/><rect x="18" y="14" width="4" height="7" rx="2"/></>,
  // star
  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>,
  // price tag
  <><path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/><circle cx="7" cy="7" r="1"/></>,
  // lightning bolt
  <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>,
  // bicycle
  <><circle cx="5.5" cy="17.5" r="3.5"/><circle cx="18.5" cy="17.5" r="3.5"/><circle cx="15" cy="5" r="1"/><path d="M12 17.5V14l-3-3 4-3 2 3h2"/></>,
  // gift box
  <><polyline points="20 12 20 22 4 22 4 12"/><rect x="2" y="7" width="20" height="5"/><line x1="12" y1="22" x2="12" y2="7"/><path d="M12 7H7.5a2.5 2.5 0 010-5C11 2 12 7 12 7z"/><path d="M12 7h4.5a2.5 2.5 0 000-5C13 2 12 7 12 7z"/></>,
  // percent
  <><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></>,
  // teddy bear
  <><circle cx="12" cy="14" r="4"/><circle cx="8" cy="10" r="2.5"/><circle cx="16" cy="10" r="2.5"/><path d="M9.5 16.5a3 3 0 005 0"/></>,
  // shoe / sneaker
  <><path d="M2 21h20"/><path d="M6 21V15l2-7h5l4 7h4a1 1 0 010 6"/><line x1="8" y1="8" x2="10" y2="15"/></>,
]

export type AuthVariant = 'blue' | 'green' | 'purple'
interface Props { variant: AuthVariant }

interface Icon {
  x: number; y: number; color: string
  idx: number; size: number; rotate: number; lit: boolean
}

const COLS = 12
const ROWS = 16

export function AuthBackground({ variant }: Props) {
  const icons = useMemo<Icon[]>(() => {
    const total    = COLS * ROWS                  // 48 background icons
    const litCount = Math.floor(rnd(5, 21))       // 5–20 randomly lit

    // Fisher-Yates to pick lit indices
    const order = Array.from({ length: total }, (_, i) => i)
    for (let i = order.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [order[i], order[j]] = [order[j], order[i]]
    }
    const litSet = new Set(order.slice(0, litCount))

    return Array.from({ length: total }, (_, i) => {
      const col = i % COLS
      const row = Math.floor(i / COLS)
      const lit = litSet.has(i)
      return {
        x:      (col + 0.5) / COLS * 100 + rnd(-4, 4),
        y:      (row + 0.5) / ROWS * 100 + rnd(-4, 4),
        color:  pick(NEONS),
        idx:    Math.floor(rnd(0, ICONS.length)),
        size:   lit ? Math.round(rnd(22, 32)) : Math.round(rnd(14, 20)),
        rotate: Math.round(rnd(-25, 25)),
        lit,
      }
    })
  }, [])

  return (
    <>
      {icons.map((ic, i) => (
        <div
          key={i}
          className="absolute pointer-events-none"
          style={{
            left:      `${ic.x}%`,
            top:       `${ic.y}%`,
            transform: `translate(-50%,-50%) rotate(${ic.rotate}deg)`,
          }}
        >
          <svg
            width={ic.size} height={ic.size} viewBox="0 0 24 24"
            strokeWidth="1.5" fill="none"
            strokeLinecap="round" strokeLinejoin="round"
            style={{
              stroke:  `rgb(var(${ic.color}))`,
              opacity: ic.lit ? 0.65 : 0.09,
              filter:  ic.lit ? glow(ic.color) : undefined,
            }}
          >
            {ICONS[ic.idx]}
          </svg>
        </div>
      ))}

      {variant === 'blue' && (
        <>
          <div className="absolute -top-32 -right-32 w-96 h-96 bg-neon-blue/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute -bottom-32 -left-32 w-80 h-80 bg-neon-purple/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[280px] bg-neon-cyan/5 rounded-full blur-3xl pointer-events-none" />
        </>
      )}
      {variant === 'green' && (
        <>
          <div className="absolute -top-32 -left-32 w-96 h-96 bg-neon-green/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute -bottom-32 -right-32 w-80 h-80 bg-neon-cyan/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute top-1/3 right-0 w-72 h-72 bg-neon-blue/5 rounded-full blur-3xl pointer-events-none" />
        </>
      )}
      {variant === 'purple' && (
        <>
          <div className="absolute -top-32 -right-32 w-96 h-96 bg-neon-purple/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute -bottom-32 -left-32 w-80 h-80 bg-neon-pink/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[280px] bg-neon-blue/5 rounded-full blur-3xl pointer-events-none" />
        </>
      )}
    </>
  )
}
