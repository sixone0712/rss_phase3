import { Global } from '@emotion/react';
import React from 'react';
import { hot } from 'react-hot-loader/root';
import { Redirect, Route, Switch } from 'react-router-dom';
import './App.css';
import PrivateRoute from './components/atoms/PrivateRoute';
import AppLayout from './components/templates/AppLayout';
import globalStyle from './globalStyle';
import { PAGE_URL } from './lib/constants';
import DashBoard from './pages/DashBoard';
import Forbidden from './pages/Forbidden';
import Login from './pages/Login';
import NotFound from './pages/NotFound';

function App() {
  return (
    <>
      <AppLayout>
        <Switch>
          <Route exact path={['/', '/logmonitor']}>
            <Redirect to={PAGE_URL.STATUS_REMOTE} />
          </Route>
          <Route path={PAGE_URL.LOGIN_ROUTE}>
            <Login />
          </Route>
          <PrivateRoute
            path={[
              PAGE_URL.STATUS_ROUTE,
              PAGE_URL.CONFIGURE_ROUTE,
              PAGE_URL.RULES_ROUTE,
              PAGE_URL.ADDRESS_BOOK_ROUTE,
              PAGE_URL.ACCOUNT_ROUTE,
            ]}
          >
            <DashBoard />
          </PrivateRoute>
          <Route path={PAGE_URL.FORBBIDEN_ROUTE}>
            <Forbidden />
          </Route>
          <Route path="*">
            <NotFound />
          </Route>
        </Switch>
      </AppLayout>
      <Global styles={globalStyle} />
    </>
  );
}

export default process.env.NODE_ENV === 'development' ? hot(App) : App;
