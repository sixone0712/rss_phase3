import { AxiosError } from 'axios';
import { createAction, createAsyncAction } from 'typesafe-actions';
import { DEVICE_INFO_LIST } from './types';

export const SYSTEM_INFO_GET_DEVICE_INFO = 'systemInfo/SYSTEM_INFO_GET_DEVICE_INFO' as const;
export const SYSTEM_INFO_GET_DEVICE_INFO_SUCCESS = 'systemInfo/SYSTEM_INFO_GET_DEVICE_INFO_SUCCESS' as const;
export const SYSTEM_INFO_GET_DEVICE_INFO_FAILURE = 'systemInfo/SYSTEM_INFO_GET_DEVICE_INFO_FAILURE' as const;
export const SYSTEM_INFO_SET_SELECTED_DEVICE = 'systemInfo/SYSTEM_INFO_SET_SELECTED_DEVICE' as const;

export const getDeviceInfoAsync = createAsyncAction(
  SYSTEM_INFO_GET_DEVICE_INFO,
  SYSTEM_INFO_GET_DEVICE_INFO_SUCCESS,
  SYSTEM_INFO_GET_DEVICE_INFO_FAILURE,
)<undefined, DEVICE_INFO_LIST[], AxiosError>();

export const systemInfoSetSelectedDevice = createAction(
  SYSTEM_INFO_SET_SELECTED_DEVICE,
)<string | null>();
