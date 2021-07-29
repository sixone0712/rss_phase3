import { ActionType } from 'typesafe-actions';
import * as actions from './actions';
import React from 'react';

export type DeviceInfoAction = ActionType<typeof actions>;

export interface RES_DEVICE_INFO_CONTAINER {
  name: string | null;
  status: string | null;
}

export interface RES_DEVICE_INFO {
  volumeUsed: string | null;
  volumeTotal: string | null;
  name: string | null;
  type: string | null;
  host: string | null;
  containers: RES_DEVICE_INFO_CONTAINER[];
}

export interface RES_SYSTEM_INFO_DEVICE {
  list: RES_DEVICE_INFO[];
}

export interface DEVICE_INFO {
  deviceInfo: {
    list: DEVICE_INFO_LIST[];
    selected: string | null;
    pending: boolean;
    success: boolean;
    failure: boolean;
    error: any;
  };
}

export interface DEVICE_INFO_LIST {
  //key: number | null;
  key: React.Key;
  name: string | null;
  type: string | null;
  ip: string | null;
  status: string[] | null;
  volume: string | null;
}
