<script setup>
import { ref, onMounted, computed } from 'vue'

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

// Dates arrive either as bare "2026-06-30" or full ISO "2026-06-30T00:00:00.000Z".
// Normalize to the calendar day so grouping and formatting are stable.
function dayKey(entry) {
  return (entry.date ?? '').slice(0, 10) || 'Unknown date'
}

// Entries grouped by day, newest day first, each group's entries sorted by repo.
const groups = computed(() => {
  const byDay = {}
  for (const entry of entries.value) {
    const key = dayKey(entry)
    ;(byDay[key] ??= []).push(entry)
  }
  return Object.keys(byDay)
    .sort((a, b) => b.localeCompare(a))
    .map((date) => ({
      date,
      entries: byDay[date].sort((a, b) =>
        (a.repository ?? '').localeCompare(b.repository ?? ''))
    }))
})

// Stable per-repository accent color so the same repo reads consistently.
const palette = ['#2563eb', '#0891b2', '#7c3aed', '#db2777', '#ea580c', '#16a34a', '#ca8a04']
function repoColor(repo) {
  let h = 0
  for (let i = 0; i < (repo ?? '').length; i++) h = (h * 31 + repo.charCodeAt(i)) >>> 0
  return palette[h % palette.length]
}

const MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

// Monthly frequency buckets spanning the full range of entries. Gaps between the
// earliest and latest month are filled with zero so the histogram reads as a true
// timeline rather than a list of only the active months.
const histogram = computed(() => {
  const counts = {}
  for (const entry of entries.value) {
    const ym = dayKey(entry).slice(0, 7)
    if (ym.length !== 7) continue
    counts[ym] = (counts[ym] ?? 0) + 1
  }
  const keys = Object.keys(counts).sort()
  if (!keys.length) return []

  const [minY, minM] = keys[0].split('-').map(Number)
  const [maxY, maxM] = keys[keys.length - 1].split('-').map(Number)
  const months = []
  let y = minY, m = minM
  while (y < maxY || (y === maxY && m <= maxM)) {
    const ym = `${y}-${String(m).padStart(2, '0')}`
    months.push({ ym, count: counts[ym] ?? 0, label: `${MONTH_NAMES[m - 1]} ${y}` })
    if (++m > 12) { m = 1; y++ }
  }
  return months
})

// Bars grouped under a per-year label so the user can navigate by year and month.
const histogramYears = computed(() => {
  const byYear = {}
  for (const bucket of histogram.value) {
    const year = bucket.ym.slice(0, 4)
    ;(byYear[year] ??= []).push(bucket)
  }
  return Object.keys(byYear).sort().map((year) => ({ year, months: byYear[year] }))
})

const maxCount = computed(() =>
  Math.max(1, ...histogram.value.map((b) => b.count)))

// Bar height as a percentage of the chart area; non-empty months keep a small
// floor so a single change is still visible against a tall month.
function barHeight(count) {
  if (!count) return 0
  return Math.max(8, Math.round((count / maxCount.value) * 100))
}

// Snap the feed to a month by scrolling to its newest day. Groups are ordered
// newest-first, so the first group whose date falls in the month is its top.
function snapToMonth(ym) {
  const group = groups.value.find((g) => g.date.startsWith(ym))
  if (!group) return
  document.getElementById('day-' + group.date)
    ?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function repoLabel(repo) {
  return (repo ?? '').replace(/^trevorism\//, '')
}

function formatDate(dateStr) {
  if (dateStr === 'Unknown date') return dateStr
  const d = new Date(dateStr + 'T00:00:00Z')
  return d.toLocaleDateString('en-US', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  })
}
</script>

<template>
  <div class="changelog-page">
    <h1 class="title">Changelog</h1>
    <p class="subtitle">A history of changes across the Trevorism platform.</p>

    <div v-if="loading" class="status">
      <va-progress-circular indeterminate color="primary"></va-progress-circular>
      <span>Loading changelog…</span>
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
      <div v-if="histogram.length" class="histogram" role="group" aria-label="Changelog frequency over time">
        <p class="histogram-hint">Click a month to jump to it</p>
        <div class="histogram-years">
          <div v-for="yr in histogramYears" :key="yr.year" class="histogram-year">
            <div class="histogram-bars">
              <button
                v-for="bucket in yr.months"
                :key="bucket.ym"
                type="button"
                class="histogram-bar"
                :class="{ 'is-empty': !bucket.count }"
                :disabled="!bucket.count"
                :title="`${bucket.label}: ${bucket.count} change${bucket.count === 1 ? '' : 's'}`"
                :aria-label="`${bucket.label}, ${bucket.count} changes`"
                @click="snapToMonth(bucket.ym)"
              >
                <span class="histogram-bar-fill" :style="{ height: barHeight(bucket.count) + '%' }"></span>
              </button>
            </div>
            <span class="histogram-year-label">{{ yr.year }}</span>
          </div>
        </div>
      </div>

      <div class="feed">
        <section
          v-for="group in groups"
          :key="group.date"
          :id="'day-' + group.date"
          class="day-group"
        >
          <h2 class="day-header">{{ formatDate(group.date) }}</h2>
        <ul class="entry-list">
          <li
            v-for="entry in group.entries"
            :key="entry.id"
            class="entry"
            :style="{ borderLeftColor: repoColor(entry.repository) }"
          >
            <span
              class="repo-tag"
              :style="{ backgroundColor: repoColor(entry.repository) }"
            >{{ repoLabel(entry.repository) }}</span>
            <p class="summary">{{ entry.summary }}</p>
          </li>
        </ul>
        </section>
      </div>
    </template>
  </div>
</template>

<style scoped>
.changelog-page {
  max-width: 760px;
  margin: 0 auto;
  padding: 2.5rem 1.25rem 4rem;
}

.title {
  text-align: center;
  font-size: 2.25rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 0.4rem;
}

.subtitle {
  text-align: center;
  color: #64748b;
  margin: 0 0 2.5rem;
  font-size: 1.02rem;
}

.status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 3rem 1rem;
  color: #64748b;
}

.status.error { color: #dc2626; }
.empty { padding: 3rem 1rem; }

.feed {
  display: flex;
  flex-direction: column;
  gap: 2.25rem;
}

.day-group {
  display: flex;
  flex-direction: column;
  scroll-margin-top: 1rem;
}

.histogram {
  margin: 0 0 2.75rem;
  padding: 1rem 1.1rem 0.75rem;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}

.histogram-hint {
  margin: 0 0 0.75rem;
  font-size: 0.78rem;
  color: #94a3b8;
  text-align: center;
}

.histogram-years {
  display: flex;
  align-items: flex-end;
  gap: 1.25rem;
}

.histogram-year {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
}

.histogram-bars {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 88px;
}

.histogram-bar {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  min-width: 0;
  height: 100%;
  padding: 0;
  background: none;
  border: none;
  border-radius: 4px 4px 0 0;
  cursor: pointer;
  transition: background 0.12s ease;
}

.histogram-bar:hover:not(.is-empty) {
  background: rgba(37, 99, 235, 0.08);
}

.histogram-bar.is-empty {
  cursor: default;
}

.histogram-bar-fill {
  display: block;
  width: 100%;
  min-height: 0;
  background: #2563eb;
  border-radius: 4px 4px 0 0;
  transition: background 0.12s ease;
}

.histogram-bar:hover:not(.is-empty) .histogram-bar-fill {
  background: #1d4ed8;
}

.histogram-year-label {
  margin-top: 0.5rem;
  padding-top: 0.4rem;
  border-top: 1px solid #e2e8f0;
  text-align: center;
  font-size: 0.8rem;
  font-weight: 600;
  color: #64748b;
}

.day-header {
  position: sticky;
  top: 0;
  z-index: 1;
  margin: 0 0 0.9rem;
  padding: 0.5rem 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: #1e293b;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(4px);
  border-bottom: 1px solid #e2e8f0;
}

.entry-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.entry {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  padding: 0.95rem 1.1rem;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-left: 4px solid #2563eb;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
  transition: box-shadow 0.15s ease, transform 0.15s ease;
}

.entry:hover {
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
  transform: translateY(-1px);
}

.repo-tag {
  align-self: flex-start;
  color: #fff;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.03em;
  text-transform: uppercase;
  padding: 0.18rem 0.55rem;
  border-radius: 999px;
}

.summary {
  margin: 0;
  color: #334155;
  line-height: 1.6;
  font-size: 0.98rem;
}
</style>
