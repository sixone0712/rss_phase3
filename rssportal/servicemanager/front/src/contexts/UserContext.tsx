import React, { createContext, Dispatch, useContext, useReducer } from 'react';
import produce from 'immer';
import { listToPermissionObj } from '../api/permission';

export const initialStatePermission = {
  manual_vftp: false,
  auto: false,
  system_log: false,
  system_restart: false,
  account: false,
  config: false,
};

interface PermissionKeys {
  [key: string]: boolean;
}

export interface UserPermission extends PermissionKeys {
  manual_vftp: boolean;
  auto: boolean;
  system_log: boolean;
  system_restart: boolean;
  account: boolean;
  config: boolean;
}

export type User = {
  username: string;
  permission: UserPermission;
};

type UserState = {
  userInfo: User;
};

const initialState: UserState = {
  userInfo: {
    username: '',
    permission: {
      ...initialStatePermission,
    },
  },
};

const UserStateContext = createContext<UserState | undefined>(undefined);

type UserAction = { type: 'SET_USER_INFO'; data: User };
type UserDispatch = Dispatch<UserAction>;

const UserDispatchContext = createContext<UserDispatch | undefined>(undefined);

function userReducer(
  state: UserState = initialState,
  action: UserAction,
): UserState {
  switch (action.type) {
    case 'SET_USER_INFO':
      return produce(state, draft => {
        draft.userInfo.username = action.data.username;
        draft.userInfo.permission = listToPermissionObj(
          (action.data.permission as unknown) as string[],
        );
      });
    default:
      return state;
  }
}

export function UserContextProvider({
  children,
}: {
  children: React.ReactNode;
}): JSX.Element {
  const [state, dispatch] = useReducer(userReducer, initialState);

  return (
    <UserDispatchContext.Provider value={dispatch}>
      <UserStateContext.Provider value={state}>
        {children}
      </UserStateContext.Provider>
    </UserDispatchContext.Provider>
  );
}

// User Custom Hook
export function useUserState(): UserState {
  const state = useContext(UserStateContext);
  if (!state) throw new Error('UserStateContext not found');
  return state;
}

export function useUserDispatch(): React.Dispatch<UserAction> {
  const dispatch = useContext(UserDispatchContext);
  if (!dispatch) throw new Error('DashBoardDispatchContext not found');
  return dispatch;
}
