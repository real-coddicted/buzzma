import { DetailsCard } from '../components/ui/DetailsCard'
import { currentUser } from '../data/mockData'

export function Profile() {
  return (
    <div className="max-w-sm">
      <h1 className="text-lg font-semibold text-ink-light-primary dark:text-ink-dark-primary mb-6">
        Profile
      </h1>
      <DetailsCard details={currentUser} />
    </div>
  )
}
