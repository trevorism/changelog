import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import ChangelogTimeline from '../src/components/ChangelogTimeline.vue'

const mockApiEntries = [
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

  it('renders API entries newest-first grouped by date', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockApiEntries
    })

    const wrapper = mount(ChangelogTimeline)
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

  it('falls back to sample data when API returns empty list', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => []
    })

    const wrapper = mount(ChangelogTimeline)
    await new Promise((r) => setTimeout(r, 50))
    await wrapper.vm.$nextTick()

    // Sample data entries should be present
    const text = wrapper.text()
    expect(text).toContain('auth-provider')
    expect(text).toContain('datastore')
    expect(text).toContain('tenant')
    expect(text).toContain('threshold-service')
    expect(text).toContain('kraken-client')

    // Should NOT show the empty state
    expect(text).not.toContain('No changelog entries yet.')
  })

  it('shows error state when fetch fails', async () => {
    global.fetch = vi.fn().mockRejectedValue(new Error('Network error'))

    const wrapper = mount(ChangelogTimeline)
    await new Promise((r) => setTimeout(r, 50))
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Error:')
  })

  it('does not show sample data when API returns entries', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockApiEntries
    })

    const wrapper = mount(ChangelogTimeline)
    await new Promise((r) => setTimeout(r, 50))
    await wrapper.vm.$nextTick()

    const text = wrapper.text()
    expect(text).not.toContain('auth-provider')
    expect(text).not.toContain('datastore')
  })
})
