import { Layout, Result } from 'antd';
import axios, { AxiosResponse } from 'axios';
import React, { useEffect, useState } from 'react';
import { openNotification } from '../../../../api/notification';
import { useDashBoardState } from '../../../../contexts/DashboardContext';
import * as DEFINE from '../../../../define';
import useAsyncAxios from '../../../../hooks/useAsyncAxios';
import {
  LogFileList,
  logFilter,
  LogType,
} from '../../../../typedef/LogDownloadType';
import LogBreadCrumb from './LogBreadCrumb';
import LogTableFileList from './LogTableFileList';
import { useSelector } from 'react-redux';
import { RootState } from '../../../../redux';

const { Content } = Layout;

const requestLogFileList = (
  device: string | null,
): Promise<AxiosResponse<any>> => {
  return axios.get(`${DEFINE.URL_DEBUG_LOG_FILES}?device=${device}`);
};

function LogTable(): JSX.Element {
  // use Context API
  // const {
  //   deviceInfo: { selected },
  // } = useDashBoardState();
  const {
    deviceInfo: { selected },
  } = useSelector((state: RootState) => state.deviceInfo);

  const [listState, listRefetch] = useAsyncAxios(
    () => requestLogFileList(selected),
    [selected],
    true,
  );
  const [fileList, setFileList] = useState<LogFileList>([]);

  useEffect(() => {
    if (listState.error) {
      openNotification(
        'error',
        'Error',
        `Failed to get file list of ${selected} due to server problem.`,
      );
    }
  }, [listState.error]);

  useEffect(() => {
    const list: any = listState.data?.data?.list || [];
    if (list.length > 0) {
      const addKeyList: LogFileList = list.map(
        (
          list: { fileName: string; fileType: string; fileSize: number },
          index: number,
        ) => {
          const fileTypeName = logFilter.find(
            item => item.value === list.fileType,
          );
          return {
            key: index,
            fileTypeName: fileTypeName?.text || '',
            ...list,
          };
        },
      );
      setFileList(addKeyList);
    } else {
      setFileList([]);
    }
  }, [listState.data]);

  const onLoadFileList = () => {
    listRefetch().then(r => r);
  };

  return (
    <Layout>
      <Content className="log-dl-table">
        <LogBreadCrumb selected={selected} />
        {!selected && <Result title="Please select a device." />}
        {selected && (
          <LogTableFileList
            selected={selected}
            fileList={fileList}
            onLoadFileList={onLoadFileList}
            listState={listState}
          />
        )}
      </Content>
    </Layout>
  );
}

export default LogTable;
