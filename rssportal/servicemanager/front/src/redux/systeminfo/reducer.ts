import produce from 'immer';
import { createReducer } from 'typesafe-actions';
import { DEVICE_INFO, DeviceInfoAction } from './types';
import {
  SYSTEM_INFO_GET_DEVICE_INFO,
  SYSTEM_INFO_GET_DEVICE_INFO_FAILURE,
  SYSTEM_INFO_GET_DEVICE_INFO_SUCCESS,
  SYSTEM_INFO_SET_SELECTED_DEVICE,
} from './actions';

const initialState: DEVICE_INFO = {
  deviceInfo: {
    list: [],
    pending: false,
    success: false,
    failure: false,
    selected: null,
    error: null,
  },
};

const reducer = createReducer<DEVICE_INFO, DeviceInfoAction>(initialState, {
  [SYSTEM_INFO_GET_DEVICE_INFO]: state =>
    produce(state, draft => {
      console.log('SYSTEM_INFO_GET_DEVICE_INFO');
      draft.deviceInfo.pending = true;
      draft.deviceInfo.success = false;
      draft.deviceInfo.failure = false;
      draft.deviceInfo.error = null;
      draft.deviceInfo.list = [];
    }),
  [SYSTEM_INFO_GET_DEVICE_INFO_SUCCESS]: (state, action) =>
    produce(state, draft => {
      console.log('SYSTEM_INFO_GET_DEVICE_INFO_SUCCESS');
      console.log('action.payload', action.payload);
      draft.deviceInfo.pending = false;
      draft.deviceInfo.success = true;
      draft.deviceInfo.failure = false;
      draft.deviceInfo.list = action.payload;
    }),
  [SYSTEM_INFO_GET_DEVICE_INFO_FAILURE]: (state, action) =>
    produce(state, draft => {
      console.log('SYSTEM_INFO_GET_DEVICE_INFO_FAILURE');
      console.log('action.payload', action.payload);
      draft.deviceInfo.pending = false;
      draft.deviceInfo.success = false;
      draft.deviceInfo.failure = true;
      draft.deviceInfo.error = action.payload;
    }),
  [SYSTEM_INFO_SET_SELECTED_DEVICE]: (state, action) =>
    produce(state, draft => {
      draft.deviceInfo.selected = action.payload;
    }),
});

export default reducer;
