// File: src/views/__tests__/RoomView.spec.js

import { describe, it, expect } from 'vitest'
import RoomView from '@/views/RoomView.vue'

const {
  convertShipsToCoordinates,
  convertShipsForApi,
  placeShipOnField,
  toggleShip
} = RoomView.methods

describe('RoomView.methods', () => {
  it('convertShipsToCoordinates правильно преобразует массив кораблей в список координат', () => {
    const ships = [
      { coords: [[0, 0], [1, 0], [2, 0]] },
      { coords: [[3, 4], [3, 5]] }
    ]
    const result = convertShipsToCoordinates.call({}, ships)
    expect(result).toEqual(
      expect.arrayContaining([
        { x: 0, y: 0 },
        { x: 1, y: 0 },
        { x: 2, y: 0 },
        { x: 3, y: 4 },
        { x: 3, y: 5 }
      ])
    )
    expect(result).toHaveLength(5)
  })

  it('convertShipsForApi возвращает массив объектов с правильными type и cells', () => {
    const ships = [
      {
        w: 4,
        h: 1,
        coords: [
          [0, 0],
          [1, 0],
          [2, 0],
          [3, 0]
        ]
      },
      {
        w: 1,
        h: 1,
        coords: [[5, 5]]
      },
      {
        w: 2,
        h: 1,
        coords: [
          [7, 2],
          [8, 2]
        ]
      }
    ]
    const result = convertShipsForApi.call({}, ships)
    // Первый корабль длины 4 → FOUR_DECK
    expect(result[0]).toEqual({
      type: 'FOUR_DECK',
      cells: [{ x: 0, y: 0 }, { x: 1, y: 0 }, { x: 2, y: 0 }, { x: 3, y: 0 }]
    })
    // Второй длины 1 → ONE_DECK
    expect(result[1]).toEqual({
      type: 'ONE_DECK',
      cells: [{ x: 5, y: 5 }]
    })
    // Третий длины 2 → TWO_DECK
    expect(result[2]).toEqual({
      type: 'TWO_DECK',
      cells: [{ x: 7, y: 2 }, { x: 8, y: 2 }]
    })
    expect(result).toHaveLength(3)
  })

  it('placeShipOnField добавляет новый корабль в this.ships и удаляет его из this.availableShips', () => {
    // Подготовим контекст: один доступный корабль и пустой список размещённых
    const fakeContext = {
      ships: [],
      availableShips: [
        {
          id: 10,
          name: '2×1',
          w: 2,
          h: 1,
          direction: 'horizontal',
          angle: 90,
          size: 2,
          // coords здесь не указываем — они вычисляются внутри метода
        }
      ],
      saveRoomState: () => {} // заглушка
    }
    // Позиционируем корабль: row=3, col=5, grabbedIndex=1
    // Тогда x0 = 5 - 1 = 4, y0 = 3. Для горизонтального двучленного корабля coords = [[4,3],[5,3]]
    placeShipOnField.call(
      fakeContext,
      { ship: fakeContext.availableShips[0], row: 3, col: 5, grabbedIndex: 1 }
    )

    // После вызова в this.ships должен появиться объект с правильными coords
    expect(fakeContext.ships).toHaveLength(1)
    const placed = fakeContext.ships[0]
    expect(typeof placed.id).toBe('number')
    expect(placed.coords).toEqual([
      [4, 3],
      [5, 3]
    ])
    // Доступный список должен стать пустым
    expect(fakeContext.availableShips).toHaveLength(0)
  })

  it('toggleShip удаляет корабль из this.ships и добавляет его обратно в this.availableShips', () => {
    // Подготовим контекст: один размещённый корабль и пустой availableShips
    const shipObj = {
      id: 42,
      name: '3×1',
      w: 3,
      h: 1,
      direction: 'horizontal',
      coords: [
        [0, 1],
        [1, 1],
        [2, 1]
      ]
    }
    const fakeContext = {
      ships: [shipObj],
      availableShips: [],
      saveRoomState: () => {}
    }

    toggleShip.call(fakeContext, shipObj)

    // После удаления ships должен быть пуст
    expect(fakeContext.ships).toHaveLength(0)
    // В availableShips должен появиться объект с тем же id, именем
    expect(fakeContext.availableShips).toHaveLength(1)
    const returned = fakeContext.availableShips[0]
    expect(returned.id).toBe(42)
    expect(returned.name).toBe('3×1')
    // Поскольку исходно direction='horizontal', w и h остаются прежними
    expect(returned.w).toBe(3)
    expect(returned.h).toBe(1)
  })
})
