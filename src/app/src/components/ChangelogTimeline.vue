<script setup>
import { ref, onMounted } from 'vue'

const entries = ref([])
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    const res = await fetch('/api/entry')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    entries.value = await res.json()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})

function groupByDate(items) {
  const grouped = {}
  items.forEach((entry) => {
    const key = entry.date ?? 'Unknown date'
    if (!grouped[key]) grouped[key] = []
    grouped[key].push(entry)
  })
  return grouped
}

const groupedEntries = () => groupByDate(entries.value)
</script>

<template>
  <div class="changelog-timeline">
    <h2 class="title">Changelog</h2>

    <div v-if="loading" class="status">Loading changelog...</div>
    <div v-else-if="error" class="status error">Error: {{ error }}</div>
    <div v-else-if="entries.length === 0" class="status empty">No changelog entries yet.</div>

    <template v-else>
      <div
        v-for="(group, date) in groupedEntries()"
        :key="date"
        class="date-group"
      >
        <h3 class="date-label">{{ date }}</h3>
        <div class="timeline">
          <div
            v-for="entry in group"
            :key="entry.id"
            class="timeline-item"
          >
            <div class="dot"></div>
            <div class="content">
              <p class="repository">{{ entry.repository }}</p>
              <p class="summary">{{ entry.summary }}</p>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.changelog-timeline {
  max-width: 700px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.title {
  text-align: center;
  font-size: 1.8rem;
  margin-bottom: 2rem;
}

.status {
  text-align: center;
  color: #666;
  font-size: 1rem;
}

.status.error {
  color: #e74c3c;
}

.empty {
  text-align: center;
  color: #999;
  font-style: italic;
}

.date-group {
  margin-bottom: 2.5rem;
}

.date-label {
  font-size: 1.2rem;
  margin-bottom: 0.75rem;
  padding-bottom: 0.4rem;
  border-bottom: 2px solid #e0e0e0;
}

.timeline {
  position: relative;
  padding-left: 1.5rem;
  border-left: 2px solid #d0d0d0;
}

.timeline-item {
  position: relative;
  display: flex;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #4a90d9;
  flex-shrink: 0;
  margin-top: 6px;
}

.content {
  flex: 1;
}

.repository {
  font-weight: 600;
  margin-bottom: 0.25rem;
  color: #333;
}

.summary {
  color: #555;
  line-height: 1.5;
}
</style>
