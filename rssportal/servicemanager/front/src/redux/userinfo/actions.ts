import { createAction } from 'typesafe-actions';
import { USER_INFO } from './types';

export const USER_INFO_SET_USER = 'userInfo/USER_INFO_SET_USER' as const;
export const USER_INFO_INIT_USER = 'userInfo/USER_INFO_INIT_USER' as const;

export const userInfoInitUser = createAction(USER_INFO_INIT_USER)();
export const userInfoSetUser = createAction(USER_INFO_SET_USER)<USER_INFO>();
