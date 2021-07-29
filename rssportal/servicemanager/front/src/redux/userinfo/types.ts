import { ActionType } from 'typesafe-actions';
import * as actions from './actions';

export type UserInfoAction = ActionType<typeof actions>;

export interface USER_INFO {
  username: string | null;
  permission: USER_INFO_PERMISSION;
}

interface PermissionKeys {
  [key: string]: boolean;
}

export interface USER_INFO_PERMISSION extends PermissionKeys {
  manual_vftp: boolean;
  auto: boolean;
  system_log: boolean;
  system_restart: boolean;
  account: boolean;
  config: boolean;
}
