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
    expect(wrapper.text()).toContain('Loading changelog')
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

    // Newest day group first: repo-a/repo-b are the 07-01 entries, repo-c is 06-30,
    // so both 07-01 repos must render before the 06-30 repo. (Dates render via
    // toLocaleDateString, which is locale/timezone-dependent, so we assert on the
    // repository ordering rather than a formatted date string.)
    const posA = text.indexOf('repo-a')
    const posB = text.indexOf('repo-b')
    const posC = text.indexOf('repo-c')
    expect(posA).toBeLessThan(posC)
    expect(posB).toBeLessThan(posC)
    // Within the 07-01 group entries sort by repository, so repo-a precedes repo-b.
    expect(posA).toBeLessThan(posB)
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
