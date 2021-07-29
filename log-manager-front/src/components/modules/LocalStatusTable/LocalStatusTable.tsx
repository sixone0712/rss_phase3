import { blue } from '@ant-design/colors';
import { DeleteOutlined } from '@ant-design/icons';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Table } from 'antd';
import React, { useCallback, useMemo } from 'react';
import { useHistory } from 'react-router-dom';
import useLocalStatus from '../../../hooks/useLocalStatus';
import { PAGE_URL } from '../../../lib/constants';
import { compareTableItem } from '../../../lib/util/compareTableItem';
import { convDateformat } from '../../../lib/util/conver';
import { BuildStatus, LocalColumnPropsType, LocalStatus } from '../../../types/status';
import PopupTip from '../../atoms/PopupTip';
import StatusBadge from '../../atoms/StatusBadge';
import StatusTableHeader from '../StatusTableHeader/StatusTableHeader';

export type LocalStatusTableProps = {};

const LocalColumnTitle = styled.div`
  font-weight: 700;
`;

const localColumnProps: LocalColumnPropsType = {
  index: {
    key: 'index',
    title: <LocalColumnTitle>No</LocalColumnTitle>,
    dataIndex: 'index',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'index'),
    },
  },
  companyFabName: {
    key: 'companyFabName',
    title: <LocalColumnTitle>User-Fab Name</LocalColumnTitle>,
    dataIndex: 'companyFabName',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'companyFabName'),
    },
  },
  status: {
    key: 'collectStatus',
    title: <LocalColumnTitle>Collect/Convert/Insert</LocalColumnTitle>,
    dataIndex: 'collectStatus',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'collectStatus'),
    },
  },
  files: {
    key: 'files',
    title: <LocalColumnTitle>Files</LocalColumnTitle>,
    dataIndex: 'files',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'files'),
    },
  },
  registeredDate: {
    key: 'registeredDate',
    title: <LocalColumnTitle>Date</LocalColumnTitle>,
    dataIndex: 'registeredDate',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'registeredDate'),
    },
  },
  delete: {
    key: 'index',
    title: <LocalColumnTitle>Delete</LocalColumnTitle>,
    align: 'center',
  },
};

export default function LocalStatusTable({}: LocalStatusTableProps): JSX.Element {
  const { localList, isFetching, refreshRemoteList, openDeleteModal } = useLocalStatus();
  const localListLen = useMemo(() => (localList?.length ? localList.length : 0), [localList?.length]);
  //const localListLen = localList?.length ? localList.length : 0;
  const history = useHistory();

  const numberRender = useCallback((value: number, record: LocalStatus, index: number) => value + 1, []);
  const buildStatusRender = useCallback((value: BuildStatus, record: LocalStatus, index: number) => {
    const onClick = () =>
      history.push(
        `${PAGE_URL.STATUS_LOCAL_BUILD_HISTORY_CONVERT}/${record.jobId}?name=${record.companyFabName}&date=${record.registeredDate}`
      );

    return <StatusBadge type={value} onClick={onClick} />;
  }, []);

  const deleteRender = useCallback(
    (value: LocalStatus, record: LocalStatus, index: number) => {
      const onClick = () => openDeleteModal(value.jobId);
      return <DeleteOutlined css={iconStyle} onClick={onClick} />;
    },
    [openDeleteModal]
  );

  const titleRender = useCallback(
    () => (
      <StatusTableHeader
        title={{
          name: 'Registered collection list',
          count: localListLen,
        }}
        addBtn={{
          name: 'Add Job',
          onClick: moveToLocalNewJob,
        }}
        refreshBtn={{
          onClick: refreshRemoteList,
        }}
        disabled={isFetching}
        isLoading={isFetching}
      />
    ),
    [localListLen, isFetching]
  );

  const filesRender = useCallback((value: string[], record: LocalStatus, index: number) => {
    return PopupTip({ value: `${value} files`, list: record.fileOriginalNames });
  }, []);

  const dateRender = useCallback((value: string, record: LocalStatus, index: number) => {
    const time = convDateformat(value);
    return <div>{time}</div>;
  }, []);

  const moveToLocalNewJob = useCallback(() => {
    history.push(PAGE_URL.STATUS_LOCAL_ADD);
  }, []);

  return (
    <Table<LocalStatus>
      rowKey={'jobId'}
      dataSource={localList}
      bordered
      title={titleRender}
      size="middle"
      pagination={{
        position: ['bottomCenter'],
        total: localListLen,
        showSizeChanger: true,
      }}
      loading={isFetching}
      css={tableStyle}
    >
      <Table.Column<LocalStatus> {...localColumnProps.index} render={numberRender} />
      <Table.Column<LocalStatus> {...localColumnProps.companyFabName} />
      <Table.Column<LocalStatus> {...localColumnProps.files} render={filesRender} />
      <Table.Column<LocalStatus> {...localColumnProps.status} render={buildStatusRender} />
      <Table.Column<LocalStatus> {...localColumnProps.registeredDate} render={dateRender} />
      <Table.Column<LocalStatus> {...localColumnProps.delete} render={deleteRender} />
    </Table>
  );
}

const tableStyle = css`
  width: 86rem;
`;

const iconStyle = css`
  font-size: 1.25rem;
  &:hover {
    color: ${blue[4]};
  }
  &:active {
    color: ${blue[6]};
  }
`;
