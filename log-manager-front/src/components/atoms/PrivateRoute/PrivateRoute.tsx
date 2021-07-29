import React, { useEffect, useState } from 'react';
import { Route, useHistory } from 'react-router-dom';
import useValidateLogin from '../../../hooks/useValidateLogin';
import { PAGE_URL } from '../../../lib/constants';

export type PrivateRouteProps = {
  path: string | string[];
  children: React.ReactNode;
};

export default function RoleRoute({ path, children }: PrivateRouteProps): JSX.Element | null {
  const [isloggedin, setLoggedIn] = useState(false);
  const history = useHistory();
  const { vaildateToken } = useValidateLogin();

  useEffect(() => {
    vaildateToken().then((result) => {
      if (result) {
        setLoggedIn(true);
      } else {
        setLoggedIn(false);
        history.push(PAGE_URL.LOGIN_ROUTE);
      }
    });
  }, []);

  if (!isloggedin) {
    return null;
  }

  return <Route path={path}>{children}</Route>;
}
