<script setup>
import { ref, onMounted, computed } from 'vue'

const props = defineProps({
  apiBase: {
    type: String,
    default: '/api/entry'
  }
})

const entries = ref([])
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    const response = await fetch(props.apiBase)
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    entries.value = await response.json()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})

const groupedEntries = computed(() => {
  const map = {}
  entries.value.forEach(entry => {
    if (!map[entry.date]) map[entry.date] = []
    map[entry.date].push(entry)
  })
  return Object.entries(map).sort((a, b) => b[0].localeCompare(a[0]))
})
</script>

<template>
  <div class="changelog-timeline">
    <va-progress-circular v-if="loading" indeterminate color="primary" />

    <div v-else-if="error" class="timeline-error">
      <va-icon name="error_outline" size="large" color="danger" />
      <p>Failed to load changelog: {{ error }}</p>
    </div>

    <div v-else-if="entries.length === 0" class="timeline-empty">
      <va-icon name="info" size="large" color="secondary" />
      <p>No changelog entries found.</p>
    </div>

    <div v-else class="timeline">
      <div
        v-for="[date, group] in groupedEntries"
        :key="date"
        class="timeline-group"
      >
        <h2 class="timeline-date">{{ date }}</h2>
        <div class="timeline-items">
          <div
            v-for="entry in group"
            :key="entry.id"
            class="timeline-item"
          >
            <div class="timeline-dot" />
            <div class="timeline-content">
              <span class="timeline-repo">{{ entry.repository }}</span>
              <p class="timeline-summary">{{ entry.summary }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.changelog-timeline {
  display: flex;
  justify-content: center;
  padding: 2rem;
}

.timeline-error,
.timeline-empty {
  text-align: center;
  padding: 3rem;
}

.timeline-error p,
.timeline-empty p {
  margin-top: 0.5rem;
  color: #666;
}

.timeline {
  max-width: 700px;
  width: 100%;
  position: relative;
  padding-left: 2rem;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e0e0e0;
}

.timeline-group {
  margin-bottom: 1.5rem;
}

.timeline-date {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 0.75rem -2rem;
  position: relative;
  z-index: 1;
}

.timeline-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  position: relative;
}

.timeline-dot {
  width: 14px;
  height: 14px;
  min-width: 14px;
  background: #6200ea;
  border-radius: 50%;
  margin-top: 4px;
  z-index: 1;
}

.timeline-content {
  flex: 1;
}

.timeline-repo {
  font-weight: 600;
  font-size: 0.875rem;
  color: #6200ea;
}

.timeline-summary {
  margin: 0.25rem 0 0 0;
  color: #333;
}
</style>
