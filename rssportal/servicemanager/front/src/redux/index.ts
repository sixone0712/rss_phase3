import { combineReducers } from 'redux';
import deviceInfo from './systeminfo/reducer';
import userInfo from './userinfo/reducer';
import { deviceInfoSaga } from './systeminfo/sagas';
import { all } from 'redux-saga/effects';

const rootReducer = combineReducers({
  deviceInfo,
  userInfo,
});

export default rootReducer;

export type RootState = ReturnType<typeof rootReducer>;

export function* rootSaga() {
  yield all([deviceInfoSaga()]);
}
