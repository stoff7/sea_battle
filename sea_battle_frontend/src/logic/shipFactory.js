export function createShip({ id, name, w, h, direction, angle, size, coords }) {
  return {
    id,
    name,
    w,
    h,
    direction,
    angle,
    size,
    coords: coords || [],
  };
}