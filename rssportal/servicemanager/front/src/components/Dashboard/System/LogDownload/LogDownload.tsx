import React from 'react';
import { Layout } from 'antd';
import LogSider from './LogSider';
import LogTable from './LogTable';

function LogDownload(): JSX.Element {
  return (
    <Layout className="log-dl-layout">
      <LogSider />
      <LogTable />
    </Layout>
  );
}

export default LogDownload;
