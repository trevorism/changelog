import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import ChangelogTimeline from '../src/components/ChangelogTimeline.vue'

const mockEntries = [
  { id: '1', date: '2026-07-01', repository: 'repo-a', summary: 'Added feature X' },
  { id: '2', date: '2026-07-01', repository: 'repo-b', summary: 'Fixed bug Y' },
  { id: '3', date: '2026-06-30', repository: 'repo-c', summary: 'Updated docs Z' }
]

describe('ChangelogTimeline', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows loading state initially', () => {
    const wrapper = mount(ChangelogTimeline)
    expect(wrapper.text()).toContain('Loading changelog...')
  })

  it('renders entries newest-first grouped by date', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockEntries
    })

    const wrapper = mount(ChangelogTimeline)

    // Wait for fetch to resolve and component to update
    await new Promise((r) => setTimeout(r, 50))
    await wrapper.vm.$nextTick()

    const text = wrapper.text()
    expect(text).toContain('repo-a')
    expect(text).toContain('Added feature X')
    expect(text).toContain('repo-b')
    expect(text).toContain('Fixed bug Y')
    expect(text).toContain('repo-c')
    expect(text).toContain('Updated docs Z')

    // 2026-07-01 should appear before 2026-06-30 (newest first)
    const date1Pos = text.indexOf('2026-07-01')
    const date0Pos = text.indexOf('2026-06-30')
    expect(date1Pos).toBeLessThan(date0Pos)
  })

  it('shows empty state when no entries returned', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => []
    })

    const wrapper = mount(ChangelogTimeline)
    await new Promise((r) => setTimeout(r, 50))
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('No changelog entries yet.')
  })

  it('shows error state when fetch fails', async () => {
    global.fetch = vi.fn().mockRejectedValue(new Error('Network error'))

    const wrapper = mount(ChangelogTimeline)
    await new Promise((r) => setTimeout(r, 50))
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Error:')
  })
})
