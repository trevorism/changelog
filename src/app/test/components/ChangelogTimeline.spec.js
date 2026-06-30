import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ChangelogTimeline from '../../src/components/ChangelogTimeline.vue'

const mockEntries = [
  { id: '1', repository: 'trevorism/platform', summary: 'Latest feature added', date: '2026-07-01' },
  { id: '2', repository: 'trevorism/changelog', summary: 'Initial release', date: '2026-06-25' },
  { id: '3', repository: 'trevorism/secure-utils', summary: 'Security fix', date: '2026-06-25' }
]

const mockEmpty = []

async function tick() {
  await new Promise(resolve => setTimeout(resolve, 10))
}

describe('ChangelogTimeline', () => {
  it('renders loading spinner initially', async () => {
    const fetchMock = vi.fn().mockReturnValue(new Promise(() => {}))
    const origFetch = global.fetch
    global.fetch = fetchMock

    const wrapper = mount(ChangelogTimeline)
    expect(wrapper.find('.changelog-timeline').exists()).toBe(true)

    global.fetch = origFetch
  })

  it('renders entries newest-first grouped by date when data is available', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockEntries)
    })
    const origFetch = global.fetch
    global.fetch = fetchMock

    const wrapper = mount(ChangelogTimeline)
    await tick()
    await tick()
    await tick()

    expect(wrapper.html()).toContain('trevorism/platform')
    expect(wrapper.html()).toContain('Latest feature added')
    expect(wrapper.html()).toContain('trevorism/changelog')
    expect(wrapper.html()).toContain('Initial release')
    expect(wrapper.html()).toContain('trevorism/secure-utils')
    expect(wrapper.html()).toContain('Security fix')

    global.fetch = origFetch
  })

  it('renders empty state when no entries are returned', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockEmpty)
    })
    const origFetch = global.fetch
    global.fetch = fetchMock

    const wrapper = mount(ChangelogTimeline)
    await tick()
    await tick()
    await tick()

    expect(wrapper.html()).toContain('No changelog entries found')

    global.fetch = origFetch
  })

  it('renders error state when fetch fails', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: false,
      status: 500
    })
    const origFetch = global.fetch
    global.fetch = fetchMock

    const wrapper = mount(ChangelogTimeline)
    await tick()
    await tick()
    await tick()

    expect(wrapper.html()).toContain('Failed to load changelog')

    global.fetch = origFetch
  })
})
