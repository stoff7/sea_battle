import { describe, it, expect } from 'vitest'
import InbattleView from '@/views/InBattleView.vue'

const { textToSize, getAdjacentCells, getRandomAvailableIndex } = InbattleView.methods

describe('InbattleView.methods', () => {
  it('textToSize maps deck identifiers to correct sizes', () => {
    expect(textToSize('FOUR_DECK')).toBe(4)
    expect(textToSize('THREE_DECK')).toBe(3)
    expect(textToSize('TWO_DECK')).toBe(2)
    expect(textToSize('ONE_DECK')).toBe(1)
  })

  it('getAdjacentCells returns correct adjacent indices for a single coordinate', () => {
    // Для coords = [{ x: 1, y: 1 }] на сетке 10×10
    // Ожидаемые индексы: 
    // (0,0)=0, (1,0)=1, (2,0)=2,
    // (0,1)=10,       (2,1)=12,
    // (0,2)=20, (1,2)=21, (2,2)=22
    const coords = [{ x: 1, y: 1 }]
    const result = getAdjacentCells.call({}, coords, 10)

    expect(result).toEqual(
      expect.arrayContaining([0, 1, 2, 10, 12, 20, 21, 22])
    )
    // Проверяем, что в результате ровно 8 элементов
    expect(result).toHaveLength(8)
  })

  it('getAdjacentCells excludes cells that coincide с самим кораблем', () => {
    // Если coords содержит, например, точку (0,0),
    // то getAdjacentCells не должен возвращать idx=0.
    const coords = [{ x: 0, y: 0 }]
    const result = getAdjacentCells.call({}, coords, 10)
    // Возможные смежные: (0,1)=10, (1,0)=1, (1,1)=11
    expect(result).toEqual(
      expect.arrayContaining([1, 10, 11])
    )
    expect(result).not.toContain(0)
  })

  it('getRandomAvailableIndex выбирает индекс вне hits и misses', () => {
    const hits = [0, 1, 2]
    const misses = [3, 4]
    // Объединённый набор занятых: {0,1,2,3,4}
    // Результат должен быть числом от 0 до 99, но не входить в {0,1,2,3,4}
    const idx = getRandomAvailableIndex.call({}, hits, misses)

    expect(typeof idx).toBe('number')
    expect(idx).toBeGreaterThanOrEqual(0)
    expect(idx).toBeLessThan(100)
    expect([0, 1, 2, 3, 4]).not.toContain(idx)
  })
})
