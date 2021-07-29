import React from 'react';
import { Breadcrumb } from 'antd';

function LogBreadCrumb({ selected }: { selected: string | null }): JSX.Element {
  return (
    <Breadcrumb className="log-dl-table-bread">
      <Breadcrumb.Item>Dashboard</Breadcrumb.Item>
      <Breadcrumb.Item>System</Breadcrumb.Item>
      <Breadcrumb.Item>Log Download</Breadcrumb.Item>
      {selected && <Breadcrumb.Item>{selected}</Breadcrumb.Item>}
    </Breadcrumb>
  );
}

export default LogBreadCrumb;
