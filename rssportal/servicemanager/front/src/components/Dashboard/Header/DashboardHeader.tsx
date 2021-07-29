import React from 'react';
import { Col, Dropdown, Layout, Menu, Row } from 'antd';
import styled from 'styled-components';

import { FaUserCircle } from 'react-icons/fa';
import { useHistory } from 'react-router-dom';
import axios from 'axios';
import * as DEFINE from '../../../define';
import { useUserDispatch, useUserState } from '../../../contexts/UserContext';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../../../redux';
import { userInfoInitUser } from '../../../redux/userinfo/actions';

const { Header } = Layout;

const BoardHeader = styled(Header)`
  height: 56px;
  box-shadow: 0 2px 4px -1px rgba(0, 0, 0, 0.2), 0 4px 5px 0 rgba(0, 0, 0, 0.14),
    0 1px 10px 0 rgba(0, 0, 0, 0.12);
  z-index: 500;
`;

const HeaderRow = styled(Row)`
  min-width: 1050px;
  height: 56px;
`;

const TitleCol = styled(Col)`
  color: white;
  line-height: 30px;
  font-size: 1.25rem;
  height: 30px;
  font-weight: bold;
`;

const UserCol = styled(Col)`
  color: white;
  height: 30px;
`;

const UserArea = styled(Row)`
  height: 30px;
  color: white;
`;

const UserMenu = styled(Menu)``;

const UserMenuItem = styled(Menu.Item)`
  width: 135px;
  text-align: center;
`;

const UserName = styled(Col)`
  line-height: 30px;
  font-size: 1rem;
  height: 30px;
  margin-left: 10px;
`;

export const menu = ({
  history,
  dispatch,
}: {
  history: any;
  dispatch: any;
}) => {
  return (
    <UserMenu>
      <UserMenuItem
        key="0"
        onClick={async () => {
          try {
            await axios.get(DEFINE.URL_LOGOUT);
          } catch (e) {
            console.error(e);
          } finally {
            // use Context API
            // dispatch({
            //   type: 'SET_USER_INFO',
            //   data: { username: '', permission: '' },
            // });
            dispatch(userInfoInitUser());
          }
          history.push(DEFINE.URL_PAGE_LOGIN);
        }}
      >
        LogOut
        {/* <a href="http://www.alipay.com/">1st menu item</a> */}
      </UserMenuItem>
    </UserMenu>
  );
};

function DashboardHeader(): JSX.Element {
  const history = useHistory();
  // use Context API
  // const dispatch = useUserDispatch();
  // const { userInfo: { username } } = useUserState();
  const dispatchRedux = useDispatch();
  const { username } = useSelector((state: RootState) => state.userInfo);

  return (
    <BoardHeader>
      <HeaderRow justify="space-between" align="middle">
        <TitleCol>Service Manager</TitleCol>
        <UserCol>
          <Dropdown
            // use Context API
            //overlay={() => menu({ history: history, dispatch: dispatch })}
            overlay={() => menu({ history: history, dispatch: dispatchRedux })}
            trigger={['click']}
          >
            <a onClick={e => e.preventDefault()}>
              <UserArea
                justify="center"
                align="middle"
                style={{ height: '30px', color: 'white' }}
              >
                <FaUserCircle size={22} />
                <UserName>{username}</UserName>
              </UserArea>
            </a>
          </Dropdown>
        </UserCol>
      </HeaderRow>
    </BoardHeader>
  );
}

export default DashboardHeader;
