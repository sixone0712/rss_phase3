import { useCallback, useState } from 'react';
import { useCookies } from 'react-cookie';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { logout } from '../lib/api/axios/requests';
import { ACCESS_TOKEN_NAME, PAGE_URL, REFRESH_TOKEN_NAME, TOKEN_PATH } from '../lib/constants';
import { initLoginUser, LoginUserSelector } from '../reducers/slices/loginUser';

export const NavType = {
  STATUS: 'status',
  STATUS_REMOTE: 'status:remote',
  STATUS_LOCAL: 'status:local',
  CONFIGURE: 'configure',
  RULES: 'rules',
  RULES_CONVERT: 'rules:convert',
  RULES_CRAS: 'rules:cras',
  ADDRESS_BOOK: 'address',
  ACCOUNT: 'account',
};
export default function useDashboardHeader() {
  const history = useHistory();
  const dispatch = useDispatch();
  const [currentKey, setCurrentKey] = useState([NavType.STATUS_REMOTE]);
  const [, , removeCookie] = useCookies([ACCESS_TOKEN_NAME, REFRESH_TOKEN_NAME]);
  const loggedInUser = useSelector(LoginUserSelector);
  const [visiblePassword, setVisiblePassword] = useState(false);

  const onLogout = () => {
    removeCookie(ACCESS_TOKEN_NAME, { path: TOKEN_PATH });
    removeCookie(REFRESH_TOKEN_NAME, { path: TOKEN_PATH });
    dispatch(initLoginUser);
    logout();
    history.replace(PAGE_URL.LOGIN_ROUTE);
  };

  const onChangePassword = () => {
    setVisiblePassword(true);
  };

  const onClickNavItem = useCallback(
    ({ key }: { key: React.Key }) => {
      switch (key) {
        case NavType.STATUS_REMOTE:
          setCurrentKey([key]);
          history.push(PAGE_URL.STATUS_REMOTE);
          break;
        case NavType.STATUS_LOCAL:
          setCurrentKey([key]);
          history.push(PAGE_URL.STATUS_LOCAL);
          break;
        case NavType.CONFIGURE:
          setCurrentKey([key]);
          history.push(PAGE_URL.CONFIGURE);
          break;
        case NavType.RULES_CONVERT:
          setCurrentKey([key]);
          history.push(PAGE_URL.RULES_CONVERT_RULES);
          break;
        case NavType.RULES_CRAS:
          setCurrentKey([key]);
          history.push(PAGE_URL.RULES_CRAS_DATA_ROUTE);
          break;
        case NavType.ADDRESS_BOOK:
          setCurrentKey([key]);
          history.push(PAGE_URL.ADDRESS_BOOK);
          break;
        case NavType.ACCOUNT:
          setCurrentKey([key]);
          history.push(PAGE_URL.ACCOUNT);
          break;
      }
    },
    [history, setCurrentKey]
  );

  return {
    onLogout,
    onChangePassword,
    currentKey,
    onClickNavItem,
    loggedInUser,
    visiblePassword,
    setVisiblePassword,
  };
}
