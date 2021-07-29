import { blue } from '@ant-design/colors';
import {
  DeleteOutlined,
  ReloadOutlined,
  EditOutlined,
  PlusOutlined,
  ExportOutlined,
  ImportOutlined,
} from '@ant-design/icons';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Empty, Space, Table } from 'antd';
import { CompareFn } from 'antd/lib/table/interface';
import { AlignType, DataIndex } from 'rc-table/lib/interface';
import React, { Key, useCallback, useEffect } from 'react';
import useCrasDataList from '../../../hooks/useCrasDataList';
import { compareTableItem } from '../../../lib/util/compareTableItem';
import { CrasDataInfo } from '../../../types/crasData';
import TableHeader from '../TableHeader';
import CrasDataAddModal from './CrasDataAddModal';

export type CrasDataStatusProps = {};

export default function CrasDataStatus({}: CrasDataStatusProps): JSX.Element {
  const {
    list,
    openDeleteModal,
    openImportModal,
    openExportModal,
    openEditModal,
    openAddModal,
    isFetching,
    refreshStatusList,
  } = useCrasDataList();

  const titleRender = useCallback(
    () => (
      <TableHeader
        title={`Registered Item : ${list?.length ?? 0}`}
        button1={{
          name: 'Add',
          icon: <PlusOutlined />,
          onClick: openAddModal,
        }}
        button2={{
          icon: <ReloadOutlined />,
          onClick: refreshStatusList,
        }}
      />
    ),
    [list, openAddModal, refreshStatusList]
  );

  const numberRender = useCallback((value: number, record: CrasDataInfo, index: number) => value + 1, []);

  const ImportRender = useCallback(
    (value: number, record: CrasDataInfo, index: number) => {
      const onClick = () => openImportModal(value);
      return <ImportOutlined css={iconStyle} onClick={onClick} />;
    },
    [openImportModal]
  );

  const exportRender = useCallback(
    (value: number, record: CrasDataInfo, index: number) => {
      const onClick = () => openExportModal(value);
      return <ExportOutlined css={iconStyle} onClick={onClick} />;
    },
    [openExportModal]
  );

  const createEditRender = useCallback(
    (value: number, record: CrasDataInfo, index: number) => {
      const onClick = () => openEditModal('create', record.siteId, record.companyFabName);
      return (
        <Space>
          <div>{value} Items</div>
          <EditOutlined css={iconStyle} onClick={onClick} />
        </Space>
      );
    },
    [openEditModal]
  );

  const judgeEditRender = useCallback(
    (value: number, record: CrasDataInfo, index: number) => {
      const onClick = () => openEditModal('judge', record.siteId, record.companyFabName);
      return (
        <Space>
          <div>{value} Items</div>
          <EditOutlined css={iconStyle} onClick={onClick} />
        </Space>
      );
    },
    [openEditModal]
  );

  const deleteRender = useCallback(
    (value: number, record: CrasDataInfo, index: number) => {
      const onClick = () => openDeleteModal(value);
      return <DeleteOutlined css={iconStyle} onClick={onClick} />;
    },
    [openDeleteModal]
  );

  return (
    <>
      <Table<CrasDataInfo>
        rowKey={'siteId'}
        dataSource={isFetching ? undefined : list}
        bordered
        title={titleRender}
        size="middle"
        pagination={{
          position: ['bottomCenter'],
          total: list?.length ?? 0,
          showSizeChanger: true,
        }}
        loading={isFetching}
        css={tableStyle}
        locale={{
          emptyText: isFetching ? <Empty description="Loading" /> : <Empty description="No Data" />,
        }}
      >
        <Table.Column<CrasDataInfo> {...statusColumnProps.index} render={numberRender} />
        <Table.Column<CrasDataInfo> {...statusColumnProps.companyFabName} />
        <Table.Column<CrasDataInfo> {...statusColumnProps.createCrasDataItemCount} render={createEditRender} />
        <Table.Column<CrasDataInfo> {...statusColumnProps.crasDataJudgeRulesItemCount} render={judgeEditRender} />
        <Table.Column<CrasDataInfo> {...statusColumnProps.date} />
        <Table.Column<CrasDataInfo> {...statusColumnProps.import} render={ImportRender} />
        <Table.Column<CrasDataInfo> {...statusColumnProps.export} render={exportRender} />
        <Table.Column<CrasDataInfo> {...statusColumnProps.delete} render={deleteRender} />
      </Table>
      <CrasDataAddModal />
    </>
  );
}

const tableStyle = css`
  width: 86rem;
`;

const iconStyle = css`
  font-size: 1rem;
  &:hover {
    color: ${blue[4]};
  }
  &:active {
    color: ${blue[6]};
  }
`;

const StatusColumnTitle = styled.div`
  font-weight: 700;
`;

type StatusColumnName =
  | 'index'
  | 'companyFabName'
  | 'createCrasDataItemCount'
  | 'crasDataJudgeRulesItemCount'
  | 'date'
  | 'import'
  | 'export'
  | 'delete';

type StatusColumnPropsType = {
  [name in StatusColumnName]: {
    key?: Key;
    title?: React.ReactNode;
    dataIndex?: DataIndex;
    align?: AlignType;
    sorter?:
      | boolean
      | CompareFn<CrasDataInfo>
      | {
          compare?: CompareFn<CrasDataInfo>;
          /** Config multiple sorter order priority */
          multiple?: number;
        };
  };
};

const statusColumnProps: StatusColumnPropsType = {
  index: {
    key: 'index',
    title: <StatusColumnTitle>No</StatusColumnTitle>,
    dataIndex: 'index',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'index'),
    },
  },
  companyFabName: {
    key: 'companyFabName',
    title: <StatusColumnTitle>User-Fab Name</StatusColumnTitle>,
    dataIndex: 'companyFabName',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'companyFabName'),
    },
  },
  createCrasDataItemCount: {
    key: 'createCrasDataItemCount',
    title: <StatusColumnTitle>Create Cras Data Item</StatusColumnTitle>,
    dataIndex: 'createCrasDataItemCount',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'createCrasDataItemCount'),
    },
  },
  crasDataJudgeRulesItemCount: {
    key: 'crasDataJudgeRulesItemCount',
    title: <StatusColumnTitle>Cras Data Judge Rules Item</StatusColumnTitle>,
    dataIndex: 'crasDataJudgeRulesItemCount',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'crasDataJudgeRulesItemCount'),
    },
  },
  date: {
    key: 'date',
    title: <StatusColumnTitle>Last Updated</StatusColumnTitle>,
    dataIndex: 'date',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'date'),
    },
  },
  import: {
    key: 'siteId',
    title: <StatusColumnTitle>Import</StatusColumnTitle>,
    align: 'center',
  },
  export: {
    key: 'siteId',
    title: <StatusColumnTitle>Export</StatusColumnTitle>,
    align: 'center',
  },
  delete: {
    key: 'siteId',
    title: <StatusColumnTitle>Delete</StatusColumnTitle>,
    align: 'center',
  },
};
