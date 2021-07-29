import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../rootReducer';

export interface UserRolesBoolean {
  isRoleJob: boolean;
  isRoleConfigure: boolean;
  isRoleRules: boolean;
  isRoleAddress: boolean;
  isRoleAccount: boolean;
}

interface LoginUserState {
  id: number;
  username: string;
  roles: UserRolesBoolean;
}

export const initialRolesState = {
  isRoleJob: false,
  isRoleConfigure: false,
  isRoleRules: false,
  isRoleAddress: false,
  isRoleAccount: false,
};

const initialState: LoginUserState = {
  id: 0,
  username: '',
  roles: {
    ...initialRolesState,
  },
};

const loginUser = createSlice({
  name: 'login',
  initialState,
  reducers: {
    initLoginUser: () => initialState,
    setLoginUser(state, action: PayloadAction<LoginUserState>) {
      const { id, username, roles } = action.payload;
      state.id = id;
      state.username = username;
      state.roles = roles;
    },
  },
});

export const { initLoginUser, setLoginUser } = loginUser.actions;

export const LoginUserSelector = (state: RootState): LoginUserState => state.loginUser;
export const LoginUserRolesSelector = (state: RootState): LoginUserState['roles'] => state.loginUser.roles;

export default loginUser.reducer;
