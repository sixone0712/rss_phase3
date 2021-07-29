import React, { useEffect } from 'react';
import { Divider, Layout } from 'antd';
import StatusTable from './StatusTable';
import LogDownload from './LogDownload/LogDownload';
import {
  loadDeviceList,
  useDashBoardDispatch,
} from '../../../contexts/DashboardContext';
import { useUserState } from '../../../contexts/UserContext';
import { useDispatch, useSelector } from 'react-redux';
import { getDeviceInfoAsync } from '../../../redux/systeminfo/actions';
import { RootState } from '../../../redux';

const { Content } = Layout;

function SystemInfo(): JSX.Element {
  // use Context API
  //const dispatch = useDashBoardDispatch();
  // const {
  //   userInfo: { permission: { system_restart, system_log } },
  // } = useUserState();
  const dispatchRedux = useDispatch();
  const {
    permission: { system_restart, system_log },
  } = useSelector((state: RootState) => state.userInfo);

  useEffect(() => {
    //loadDeviceList(dispatch).then(r => r);    // use Context API
    dispatchRedux(getDeviceInfoAsync.request());
  }, []);

  return (
    <div className="system-info">
      {system_restart && (
        <Content>
          <Layout>
            <Content className="status-table">
              <StatusTable />
            </Content>
          </Layout>
        </Content>
      )}
      {system_restart && system_log && (
        <Divider plain className="content-divider-top" />
      )}
      {system_log && (
        <Content>
          <Layout>
            <Content className="system-log">
              <LogDownload />
            </Content>
          </Layout>
        </Content>
      )}
      <Divider plain className="content-divider-bottom" />
    </div>
  );
}

export default SystemInfo;
