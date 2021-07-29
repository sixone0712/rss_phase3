import React, { useEffect, useState } from 'react';
import { Breadcrumb, Button, Col, Layout, Row, Table } from 'antd';
import { RedoOutlined, SyncOutlined } from '@ant-design/icons';
import styled from 'styled-components';
import {
  loadDeviceList,
  useDashBoardDispatch,
  useDashBoardState,
} from '../../../contexts/DashboardContext';
import OsRestartModal from './OsRestartModal';
import { execDockerRestart } from '../../../api/restart';
import { BsFillCircleFill } from 'react-icons/bs';
import { openNotification } from '../../../api/notification';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../../../redux';
import { DEVICE_INFO_LIST } from '../../../redux/systeminfo/types';
import { getDeviceInfoAsync } from '../../../redux/systeminfo/actions';
import classNames from 'classnames';

const { Column } = Table;

// use Context API
// export type DockerStatus = {
//   key: React.Key;
//   type: string;
//   name: string;
//   ip: string;
//   status: string[];
//   volume: string;
// };

export type DockerStatus = DEVICE_INFO_LIST;

export type DockerStatusList = DockerStatus[];

const convStatusColor = (value: string) => {
  let status = 'unknown';
  if (value.includes('Up')) {
    status = 'up';
  } else if (value.includes('Exited')) {
    status = 'exited';
  }
  return status;
};

function StatusTable(): JSX.Element {
  // use Context API
  // console.log(useDashBoardState());
  // const {
  //   deviceInfo: { list, success, error, failure, pending, selected },
  // } = useDashBoardState();
  const {
    deviceInfo: { list, success, error, failure, pending, selected },
  } = useSelector((state: RootState) => state.deviceInfo);

  // use Context API
  // const dispatch = useDashBoardDispatch();
  const dispatchRedux = useDispatch();
  const [osModalVisible, setOsModalVisible] = useState(false);
  const [targetDevice, setTargetDevice] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState<number>(1);

  useEffect(() => {
    if (error) {
      openNotification(
        'error',
        'Error',
        `Failed to get "Device Status" due to server problem.`,
      );
    }
  }, [error]);

  const onRefresh = () => {
    setCurrentPage(1);
    //loadDeviceList(dispatch).then(r => r);    // use Context API
    dispatchRedux(getDeviceInfoAsync.request());
  };

  const renderStatus = (
    text: string[],
    record: DockerStatus,
    index: number,
  ) => {
    // console.log('name', text);
    // console.log('record', record);
    // console.log('index', index);
    return (
      <>
        {text.map((item: string, idx: number) => (
          <Row justify="center" align="middle" key={idx}>
            <Col className={classNames('status-color', convStatusColor(item))}>
              <BsFillCircleFill />
            </Col>
            <Col className="status-color-text">{item}</Col>
          </Row>
        ))}
      </>
    );
  };

  const renderDockerRestart = (
    text: string,
    record: DockerStatus,
    index: number,
  ) => {
    return (
      <RedoOutlined
        onClick={() => {
          setTargetDevice(text);
          onDockerRestart(text);
        }}
      />
    );
  };

  const renderOsRestart = (
    text: string,
    record: DockerStatus,
    index: number,
  ) => {
    // console.log('OSRender');
    // console.log('text', text);
    // console.log('record', record);
    // console.log('index', index);
    return (
      <RedoOutlined
        onClick={() => {
          setTargetDevice(text);
          onOsRestart(text);
        }}
      />
    );
  };

  const onDockerRestart = (text: string) => {
    execDockerRestart(text, onRefresh);
  };

  const onOsRestart = (text: string) => {
    setOsModalVisible(true);
  };

  return (
    <Layout className="status-table-layout">
      <Breadcrumb className="status-table-bread">
        <Breadcrumb.Item>Dashboard</Breadcrumb.Item>
        <Breadcrumb.Item>System</Breadcrumb.Item>
        <Breadcrumb.Item>Device Status</Breadcrumb.Item>
      </Breadcrumb>
      <Row justify="end" className="status-table-button-row">
        <Button
          className="refresh-button"
          type="primary"
          icon={<SyncOutlined />}
          onClick={onRefresh}
          disabled={pending}
        >
          Reload
        </Button>
      </Row>
      <Table
        rowKey={(record: DockerStatus) => record.key}
        tableLayout="fixed"
        size="small"
        bordered
        dataSource={pending ? [] : list}
        pagination={{
          pageSize: 4,
          position: ['bottomCenter'],
          current: currentPage,
          defaultCurrent: 1,
          onChange: (page, pageSize) => {
            setCurrentPage(page);
          },
        }}
        loading={pending}
      >
        <Column
          title="Name"
          dataIndex="name"
          key="name"
          align="center"
          width="15%"
        />
        <Column
          title="IP Address"
          dataIndex="ip"
          key="ip"
          align="center"
          width="15%"
        />
        <Column
          title="Docker Status"
          dataIndex="status"
          key="status"
          align="center"
          width="35%"
          render={renderStatus}
        />
        <Column
          title="Volume"
          dataIndex="volume"
          key="volume"
          align="center"
          width="15%"
        />
        <Column
          title="Docker Restart"
          dataIndex="name"
          key="name"
          align="center"
          width="15%"
          render={renderDockerRestart}
        />
        <Column
          title="OS Restart"
          dataIndex="name"
          key="name"
          align="center"
          width="15%"
          render={renderOsRestart}
        />
      </Table>
      <OsRestartModal
        visible={osModalVisible}
        setVisible={setOsModalVisible}
        targetDevice={targetDevice}
      />
    </Layout>
  );
}

export default StatusTable;
