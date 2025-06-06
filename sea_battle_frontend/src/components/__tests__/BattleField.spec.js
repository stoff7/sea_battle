import { mount } from '@vue/test-utils'
import BattleField from '../BattleField.vue'

describe('BattleField', () => {
  it('renders ships', () => {
    const wrapper = mount(BattleField, {
      props: {
        ships: [
          { id: 1, coords: [[0,0],[0,1]], w: 2, h: 1, direction: 'vertical' }
        ],
        availableShips: [],
        ready: false
      }
    })
    expect(wrapper.findAll('.ship').length).toBe(1)
  })
})