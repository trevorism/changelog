<script setup>
import { ref, onMounted } from 'vue'

const entries = ref([])
const loading = ref(true)
const error = ref(null)

const sampleEntries = [
  {
    id: 's1',
    date: '2026-07-02',
    repository: 'auth-provider',
    summary: 'Added OAuth2 PKCE flow for third-party login and refreshed JWT token rotation logic.'
  },
  {
    id: 's2',
    date: '2026-07-02',
    repository: 'tenant',
    summary: 'Tenant onboarding wizard now supports SSO configuration via the admin panel.'
  },
  {
    id: 's3',
    date: '2026-07-01',
    repository: 'datastore',
    summary: 'Migrated to Cassandra 5.x with improved query batching for bulk inserts.'
  },
  {
    id: 's4',
    date: '2026-07-01',
    repository: 'threshold-service',
    summary: 'Added Slack webhook integration for threshold breach notifications.'
  },
  {
    id: 's5',
    date: '2026-06-30',
    repository: 'auth-provider',
    summary: 'Fixed race condition in session cleanup that caused stale tokens after logout.'
  },
  {
    id: 's6',
    date: '2026-06-30',
    repository: 'kraken-client',
    summary: 'Upgraded to Kraken SDK 1.2 with rate-limiting and circuit-breaker support.'
  }
]

onMounted(async () => {
  try {
    const res = await fetch('/api/entry')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    entries.value = Array.isArray(data) ? data : []
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }

  if (entries.value.length === 0 && !error.value) {
    entries.value = [...sampleEntries]
  }
})

function groupByDate(items) {
  const grouped = {}
  items.forEach((entry) => {
    const key = entry.date ?? 'Unknown date'
    if (!grouped[key]) grouped[key] = []
    grouped[key].push(entry)
  })
  return Object.keys(grouped)
    .sort((a, b) => b.localeCompare(a))
    .reduce((obj, key) => {
      obj[key] = grouped[key]
      return obj
    }, {})
}

const groupedEntries = () => groupByDate(entries.value)

const chipColors = ['success', 'info', 'warning', 'danger']
function getChipColor(index) {
  return chipColors[index % chipColors.length]
}

function formatDate(dateStr) {
  const d = new Date(dateStr + 'T00:00:00Z')
  return d.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
}
</script>

<template>
  <div class="changelog-timeline">
    <h2 class="title">Changelog</h2>

    <div v-if="loading" class="status">
      <va-progress-circular indeterminate color="primary"></va-progress-circular>
      <span>Loading changelog...</span>
    </div>

    <div v-else-if="error" class="status error">
      <va-alert color="danger" closable>
        <p>Error: {{ error }}</p>
      </va-alert>
    </div>

    <div v-else-if="entries.length === 0" class="status empty">
      <va-empty-state
        title="No changelog entries yet."
        description="Entries will appear here once the backend is populated."
      ></va-empty-state>
    </div>

    <template v-else>
      <va-timeline class="timeline-root" size="small">
        <va-timeline-item
          v-for="(group, date) in groupedEntries()"
          :key="date"
          :title="formatDate(date)"
          divider
        >
          <p class="raw-date">{{ date }}</p>
          <div
            v-for="(entry, idx) in group"
            :key="entry.id"
            class="timeline-card"
          >
            <va-card class="entry-card">
              <va-card-content>
                <div class="entry-header">
                  <va-chip :color="getChipColor(idx)" size="small" class="repo-chip">
                    {{ entry.repository }}
                  </va-chip>
                  <span class="entry-id">#{{ entry.id }}</span>
                </div>
                <p class="summary">{{ entry.summary }}</p>
              </va-card-content>
            </va-card>
          </div>
        </va-timeline-item>
      </va-timeline>
    </template>
  </div>
</template>

<style scoped>
.changelog-timeline {
  max-width: 780px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.title {
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 2.5rem;
}

.status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 3rem 1rem;
  color: #64748b;
  font-size: 1rem;
}

.status.error {
  color: #dc2626;
}

.empty {
  display: flex;
  justify-content: center;
  padding: 3rem 1rem;
}

.timeline-root {
  max-width: 780px;
  margin: 0 auto;
}

.raw-date {
  font-weight: 600;
  color: #475569;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.repo-chip {
  font-weight: 600;
  letter-spacing: 0.02em;
}

.summary {
  color: #334155;
  line-height: 1.65;
  margin-top: 0.5rem;
}
</style>
