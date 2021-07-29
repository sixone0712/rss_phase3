import React, { useEffect } from 'react';
import './App.css';
import { Redirect, Route, Switch, useHistory } from 'react-router-dom';
import { useUserDispatch, useUserState } from './contexts/UserContext';
import Login from './components/Login/Login';
import Dashboard from './components/Dashboard';
import NotFound from './pages/404';
import axios from 'axios';
import * as DEFINE from './define';
import axiosConfig from './api/axiosConfig';
import { useDispatch } from 'react-redux';
import { userInfoSetUser } from './redux/userinfo/actions';
import './styles/styles.scss';

axiosConfig();

function App(): JSX.Element {
  const history = useHistory();
  //const dispatch = useUserDispatch();   // use Context API
  const dispatchRedux = useDispatch();

  useEffect(() => {
    const isValidLogin = async () => {
      try {
        const response = await axios.get(DEFINE.URL_ME);
        const { username, permission } = response.data;
        //dispatch({ type: 'SET_USER_INFO', data: { username, permission } });    // use Context API
        dispatchRedux(userInfoSetUser({ username, permission }));
      } catch (e) {
        history.push(DEFINE.URL_PAGE_LOGIN);
      }
    };
    isValidLogin().then(_ => _);
  }, []);

  return (
    <div className="App">
      <Switch>
        <Route path={DEFINE.URL_PAGE_ROOT} exact component={RootComponent} />
        <Route path={DEFINE.URL_PAGE_LOGIN} component={Login} />
        <Route path={DEFINE.URL_PAGE_DASHBOARD} component={Dashboard} />
        <Route path={DEFINE.URL_PAGE_NOT_FOUND} component={NotFound} />
        <Redirect path="*" to={DEFINE.URL_PAGE_NOT_FOUND} />
      </Switch>
    </div>
  );
}

export default App;

function RootComponent(): JSX.Element {
  const history = useHistory();
  const { userInfo } = useUserState();

  useEffect(() => {
    if (userInfo.username) {
      history.push(DEFINE.URL_PAGE_DASHBOARD_SYSTEM);
    }
  }, [userInfo.username]);

  return <></>;
}
