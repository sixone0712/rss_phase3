import { applyMiddleware, createStore } from 'redux';
import createSagaMiddleware from 'redux-saga';
import { composeWithDevTools } from 'redux-devtools-extension';
import rootReducer, { rootSaga } from './index';

const sagaMiddleware = createSagaMiddleware();
const composeMiddleware =
  process.env.REACT_APP_ENV === 'development'
    ? composeWithDevTools(applyMiddleware(sagaMiddleware))
    : applyMiddleware(sagaMiddleware);
export const store = createStore(rootReducer, composeMiddleware);
sagaMiddleware.run(rootSaga);
