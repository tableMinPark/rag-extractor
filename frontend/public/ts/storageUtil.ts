export const getRole = () => {
  const role = localStorage.getItem('role')
  if (role) {
    return role
  } else {
    ;('')
  }
}

export const getUserName = () => {
  const username = localStorage.getItem('username')
  if (username) {
    return username
  } else {
    ;('')
  }
}
