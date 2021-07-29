import React, { useCallback, useEffect } from 'react';
import { Layout, Menu, Spin } from 'antd';
import {
  useDashBoardDispatch,
  useDashBoardState,
} from '../../../../contexts/DashboardContext';
import { DatabaseOutlined } from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import { systemInfoSetSelectedDevice } from '../../../../redux/systeminfo/actions';
import { RootState } from '../../../../redux';

const { Sider } = Layout;

function LogSider(): JSX.Element {
  // use Context API
  // const dispatch = useDashBoardDispatch();
  // const {
  //   deviceInfo: { list, success, error, failure, pending, selected },
  // } = useDashBoardState();
  const dispatchRedux = useDispatch();
  const {
    deviceInfo: { list, success, error, failure, pending, selected },
  } = useSelector((state: RootState) => state.deviceInfo);

  const onClick = ({
    item,
    key,
    keyPath,
    domEvent,
  }: {
    key: React.Key;
    keyPath: React.Key[];
    item: React.ReactInstance;
    domEvent: React.MouseEvent<HTMLElement>;
  }) => {
    // console.log('item', item);
    // console.log('key', key);
    // console.log('key', typeof key);
    // console.log('keyPath', keyPath);
    // console.log('domEvent', domEvent);

    // use Context API
    // dispatch({
    //   type: 'SELECT_DEVICE',
    //   selected: typeof key === 'number' ? JSON.stringify(key) : key,
    // });
    dispatchRedux(
      systemInfoSetSelectedDevice(
        typeof key === 'number' ? JSON.stringify(key) : key,
      ),
    );
  };

  useEffect(() => {
    // When the device list is updated, check if selected device is in the device list
    // If the selected device is not in the device list, it is changed to null.
    if (!list.find(item => item.name === selected)) {
      // use Context API
      // dispatch({
      //   type: 'SELECT_DEVICE',
      //   selected: null,
      // });
      dispatchRedux(systemInfoSetSelectedDevice(null));
    }
  }, [list]);

  return (
    <Sider theme="light" width={250} className="log-dl-sider">
      {pending ? (
        <div className="log-dl-sider-loading">
          <Spin size="large" />
        </div>
      ) : (
        <Menu
          theme="light"
          mode="inline"
          className="log-dl-sider-menu"
          onClick={onClick}
        >
          {list &&
            list.map(device => (
              <Menu.Item icon={<DatabaseOutlined />} key={device.name}>
                {device.name}
              </Menu.Item>
            ))}
        </Menu>
      )}
    </Sider>
  );
}

export default LogSider;
