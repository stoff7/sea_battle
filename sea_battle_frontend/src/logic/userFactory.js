export function createUser({ playerId, username, hostId, hostName, role }) {
  return {
    playerId,
    username,
    hostId,
    hostName,
    role,
  };
}