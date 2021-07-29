import { getDeviceInfoAsync, SYSTEM_INFO_GET_DEVICE_INFO } from './actions';
import { RES_DEVICE_INFO, RES_SYSTEM_INFO_DEVICE } from './types';
import { call, put, takeEvery } from 'redux-saga/effects';
import axios from 'axios';
import * as DEFINE from '../../define';
import { convDeviceInfo } from './utils';

async function getDeviceInfoAxios() {
  const response = await axios.get<RES_SYSTEM_INFO_DEVICE>(DEFINE.URL_SYSTEM);
  return response?.data?.list;
}

export function* getDeviceInfoSaga(
  action: ReturnType<typeof getDeviceInfoAsync.request>,
) {
  try {
    const deviceInfo: RES_DEVICE_INFO[] = yield call(getDeviceInfoAxios);
    const newDeviceInfo = convDeviceInfo(deviceInfo);
    yield put(getDeviceInfoAsync.success(newDeviceInfo));
  } catch (e) {
    yield put(getDeviceInfoAsync.failure(e));
  }
}

export function* deviceInfoSaga() {
  yield takeEvery(SYSTEM_INFO_GET_DEVICE_INFO, getDeviceInfoSaga);
}
