import { createReducer } from 'typesafe-actions';
import { USER_INFO, UserInfoAction } from './types';
import { USER_INFO_INIT_USER, USER_INFO_SET_USER } from './actions';
import produce from 'immer';
import { listToPermissionObj } from '../../api/permission';

export const initialState: USER_INFO = {
  username: '',
  permission: {
    manual_vftp: false,
    auto: false,
    system_log: false,
    system_restart: false,
    account: false,
    config: false,
  },
};

const reducer = createReducer<USER_INFO, UserInfoAction>(initialState, {
  [USER_INFO_INIT_USER]: (state: USER_INFO, action) =>
    produce(state, draft => {
      draft = initialState;
    }),
  [USER_INFO_SET_USER]: (state: USER_INFO, action) =>
    produce(state, draft => {
      const { username, permission } = action.payload;
      draft.username = username;
      draft.permission = listToPermissionObj(
        (permission as unknown) as string[],
      );
    }),
});

export default reducer;
