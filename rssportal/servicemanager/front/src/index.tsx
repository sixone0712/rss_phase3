import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import { BrowserRouter } from 'react-router-dom';
import { DashBoardContextProvider } from './contexts/DashboardContext';
import { UserContextProvider } from './contexts/UserContext';
import { store } from './redux/store';
import { Provider } from 'react-redux';
//import * as serviceWorker from './serviceWorker';
// import { compose } from 'redux';

// use Context API
// const Provider = compose<JSX.Element>(
//   DashBoardContextProvider,
//   UserContextProvider,
// );

const AppProvider = ({
  contexts,
  children,
}: {
  contexts: any;
  children: JSX.Element;
}) =>
  contexts.reduce(
    (prev: any, context: any) =>
      React.createElement(context, {
        children: prev,
      }),
    children,
  );

ReactDOM.render(
  //<React.StrictMode>
  <BrowserRouter>
    <Provider store={store}>
      <AppProvider contexts={[DashBoardContextProvider, UserContextProvider]}>
        <App />
      </AppProvider>
    </Provider>
  </BrowserRouter>,
  //</React.StrictMode>,
  document.getElementById('root'),
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
//serviceWorker.unregister();
