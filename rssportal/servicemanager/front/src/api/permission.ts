import {
  initialStatePermission,
  UserPermission,
} from '../contexts/UserContext';

export const listToPermissionObj = (list: string[]): UserPermission => {
  const objPermission: UserPermission = {
    ...initialStatePermission,
  };

  for (const value of list) {
    objPermission[value] = true;
  }

  return objPermission;
};
