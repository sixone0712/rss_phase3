import { combineReducers } from '@reduxjs/toolkit';
import localJob from './slices/localJob';
import remoteJob from './slices/remoteJob';
import configure from './slices/configure';
import buildHistory from './slices/buildHistory';
import loginUser from './slices/loginUser';
import address from './slices/address';
import crasData from './slices/crasData';

const rootReducer = combineReducers({
  localJob,
  remoteJob,
  configure,
  buildHistory,
  loginUser,
  address,
  crasData,
});

export default rootReducer;

export type RootState = ReturnType<typeof rootReducer>;
