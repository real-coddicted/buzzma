import { useState, useEffect, useRef } from 'react'
import { Button } from './Button'
import { fetchTicketComments, postTicketComment } from '../../api/ticketApi'
import type { Ticket, TicketComment } from '../../types/TicketTypes'

const canComment = (status: Ticket['status']) => status === 'Open' || status === 'InProgress'

function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

interface Props {
  ticket: Ticket
}

export function TicketComments({ ticket }: Props) {
  const [comments, setComments] = useState<TicketComment[]>([])
  const [commentsLoading, setCommentsLoading] = useState(true)
  const [commentsError, setCommentsError] = useState(false)
  const [newMessage, setNewMessage] = useState('')
  const [posting, setPosting] = useState(false)
  const listEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    setCommentsLoading(true)
    setCommentsError(false)
    fetchTicketComments(ticket.id)
      .then(setComments)
      .catch(() => setCommentsError(true))
      .finally(() => setCommentsLoading(false))
  }, [ticket.id])

  useEffect(() => {
    listEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [comments])

  async function handlePost() {
    const msg = newMessage.trim()
    if (!msg) return
    setPosting(true)
    try {
      const comment = await postTicketComment(ticket.id, msg)
      setComments(prev => [...prev, comment])
      setNewMessage('')
    } finally {
      setPosting(false)
    }
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handlePost()
    }
  }

  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card flex flex-col overflow-hidden">
      {/* Header */}
      <div className="px-5 py-4 border-b border-surface-light-border dark:border-surface-dark-border flex-shrink-0">
        <h2 className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Comments
          {!commentsLoading && !commentsError && (
            <span className="ml-2 text-[11px] font-normal text-ink-light-muted dark:text-ink-dark-muted">
              {comments.length}
            </span>
          )}
        </h2>
      </div>

      {/* Scrollable list */}
      <div className="flex-1 overflow-y-auto px-5 py-4">
        {commentsLoading ? (
          <div className="space-y-3">
            {[1, 2, 3].map(i => (
              <div key={i} className="h-14 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover animate-pulse" />
            ))}
          </div>
        ) : commentsError ? (
          <p className="text-xs text-neon-red">Failed to load comments.</p>
        ) : comments.length === 0 ? (
          <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted py-4 text-center">
            No comments yet.
          </p>
        ) : (
          <div className="space-y-3">
            {comments.map(c => {
              const isSupport = c.role === 'support'
              return (
                <div
                  key={c.id}
                  className={[
                    'rounded-lg px-3 py-2.5 border text-xs',
                    isSupport
                      ? 'bg-neon-blue/5 border-neon-blue/20'
                      : 'bg-surface-light-hover dark:bg-surface-dark-hover border-surface-light-border dark:border-surface-dark-border',
                  ].join(' ')}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className={['font-semibold', isSupport ? 'text-neon-blue' : 'text-ink-light-primary dark:text-ink-dark-primary'].join(' ')}>
                      {c.userName}
                    </span>
                    <span className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                      {formatDateTime(c.createdAt)}
                    </span>
                  </div>
                  <p className="text-ink-light-secondary dark:text-ink-dark-secondary leading-relaxed">
                    {c.message}
                  </p>
                </div>
              )
            })}
            <div ref={listEndRef} />
          </div>
        )}
      </div>

      {/* Compose box — only for Open / InProgress */}
      {canComment(ticket.status) && (
        <div className="px-5 py-4 border-t border-surface-light-border dark:border-surface-dark-border flex-shrink-0 space-y-2">
          <textarea
            rows={2}
            className={[
              'w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover',
              'border-surface-light-border dark:border-surface-dark-border',
              'text-xs text-ink-light-primary dark:text-ink-dark-primary',
              'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted',
              'px-3 py-2 outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all resize-none',
            ].join(' ')}
            placeholder="Write a comment… (Enter to send, Shift+Enter for new line)"
            value={newMessage}
            onChange={e => setNewMessage(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={posting}
          />
          <div className="flex justify-end">
            <Button
              variant="primary"
              size="sm"
              loading={posting}
              disabled={!newMessage.trim()}
              onClick={handlePost}
            >
              Send
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}
