import { blue } from '@ant-design/colors';
import { DeleteOutlined, EditOutlined } from '@ant-design/icons';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Table } from 'antd';
import React, { useCallback, useMemo } from 'react';
import useRemoteStatus from '../../../hooks/useRemoteStatus';
import { compareTableItem } from '../../../lib/util/compareTableItem';
import { BuildStatus, RemoteColumnPropsType, RemoteJobStatus, StatusStepType } from '../../../types/status';
import CustomIcon from '../../atoms/CustomIcon';
import StatusBadge from '../../atoms/StatusBadge';
import StatusTableHeader from '../StatusTableHeader/StatusTableHeader';

export type RemoteStatusTableProps = {};

export default function RemoteStatusTable(): JSX.Element {
  const {
    remoteList,
    isFetching,
    refreshRemoteList,
    moveToRemoteNewJob,
    moveToRemoteEditJob,
    moveToRemoteHistory,
    openStartStopModal,
    openDeleteModal,
    openEditeModal,
    loggedInUser,
  } = useRemoteStatus();
  const remoteListLen = useMemo(() => (remoteList?.length ? remoteList.length : 0), [remoteList?.length]);
  const numberRender = useCallback((value: number, record: RemoteJobStatus, index: number) => value + 1, []);

  const buildStatusRender = useCallback(
    (value: BuildStatus, record: RemoteJobStatus, index: number, type?: StatusStepType) => {
      return (
        <StatusBadge
          type={value}
          onClick={() => moveToRemoteHistory(record.jobId, record.companyFabName, type as StatusStepType)}
        />
      );
    },
    [moveToRemoteHistory]
  );

  const collectStatusRender = useCallback(
    (value: BuildStatus, record: RemoteJobStatus, index: number) => buildStatusRender(value, record, index, 'convert'),
    [buildStatusRender]
  );

  const errorSummaryStatusRender = useCallback(
    (value: BuildStatus, record: RemoteJobStatus, index: number) => buildStatusRender(value, record, index, 'error'),
    [buildStatusRender]
  );

  const crasDataStatusRender = useCallback(
    (value: BuildStatus, record: RemoteJobStatus, index: number) => buildStatusRender(value, record, index, 'cras'),
    [buildStatusRender]
  );

  const mpaVersionStatusRender = useCallback(
    (value: BuildStatus, record: RemoteJobStatus, index: number) => buildStatusRender(value, record, index, 'version'),
    [buildStatusRender]
  );

  const startAndStopRender = useCallback(
    (value: boolean, record: RemoteJobStatus, index: number) => {
      const { jobId, siteId, companyFabName, stop: prevStop } = record;
      if (value)
        return (
          <div css={statusIconStyle(loggedInUser.roles.isRoleJob)}>
            <CustomIcon className="stopped" name="stop" />
            <span
              className="text"
              onClick={() => openStartStopModal({ action: 'start', jobId, siteId, companyFabName, prevStop })}
            >
              Stopped
            </span>
          </div>
        );
      else
        return (
          <div
            css={statusIconStyle(loggedInUser.roles.isRoleJob)}
            onClick={() => openStartStopModal({ action: 'stop', jobId, siteId, companyFabName, prevStop })}
          >
            <CustomIcon className="running" name="play" />
            <span className="text">Running</span>
          </div>
        );
    },
    [openStartStopModal, loggedInUser]
  );

  const editRender = useCallback(
    (value: number, record: RemoteJobStatus, index: number) => {
      const { jobId, siteId, companyFabName, stop: prevStop } = record;
      return loggedInUser.roles.isRoleJob ? (
        <EditOutlined css={iconStyle} onClick={() => openEditeModal({ jobId, siteId, companyFabName, prevStop })} />
      ) : (
        <div>-</div>
      );
    },
    [moveToRemoteEditJob, loggedInUser]
  );

  const deleteRender = useCallback(
    (value: number, record: RemoteJobStatus, index: number) => {
      const { jobId, siteId, companyFabName, stop: prevStop } = record;
      return loggedInUser.roles.isRoleJob ? (
        <DeleteOutlined css={iconStyle} onClick={() => openDeleteModal({ jobId, siteId, companyFabName, prevStop })} />
      ) : (
        <div>-</div>
      );
    },
    [openDeleteModal, loggedInUser]
  );

  const titleRender = useCallback(
    () => (
      <StatusTableHeader
        title={{
          name: 'Registered collection list',
          count: remoteListLen,
        }}
        addBtn={
          loggedInUser.roles.isRoleJob
            ? {
                name: 'Add Job',
                onClick: moveToRemoteNewJob,
              }
            : undefined
        }
        refreshBtn={{
          onClick: refreshRemoteList,
        }}
        disabled={isFetching}
        isLoading={isFetching}
      />
    ),
    [remoteListLen, isFetching, loggedInUser]
  );

  return (
    <Table<RemoteJobStatus>
      rowKey={'jobId'}
      dataSource={remoteList}
      bordered
      title={titleRender}
      size="middle"
      pagination={{
        position: ['bottomCenter'],
        total: remoteListLen,
        showSizeChanger: true,
      }}
      loading={isFetching}
      css={tableStyle}
    >
      <Table.Column<RemoteJobStatus> {...remoteColumnProps.index} render={numberRender} />
      <Table.Column<RemoteJobStatus> {...remoteColumnProps.companyFabName} />
      <Table.Column<RemoteJobStatus> {...remoteColumnProps.collectStatus} render={collectStatusRender} />
      <Table.Column<RemoteJobStatus> {...remoteColumnProps.errorSummaryStatus} render={errorSummaryStatusRender} />
      <Table.Column<RemoteJobStatus> {...remoteColumnProps.crasDataStatus} render={crasDataStatusRender} />
      <Table.Column<RemoteJobStatus> {...remoteColumnProps.mpaVersionStatus} render={mpaVersionStatusRender} />
      <Table.Column<RemoteJobStatus> {...remoteColumnProps.stop} render={startAndStopRender} />
      <Table.Column<RemoteJobStatus> {...remoteColumnProps.edit} render={editRender} />
      <Table.Column<RemoteJobStatus> {...remoteColumnProps.delete} render={deleteRender} />
    </Table>
  );
}

const tableStyle = css`
  width: 86rem;
`;

const ColumnTitle = styled.div`
  font-weight: 700;
`;

const remoteColumnProps: RemoteColumnPropsType = {
  index: {
    key: 'index',
    title: <ColumnTitle>No</ColumnTitle>,
    dataIndex: 'index',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'index'),
    },
  },
  companyFabName: {
    key: 'companyFabName',
    title: <ColumnTitle>User-Fab Name</ColumnTitle>,
    dataIndex: 'companyFabName',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'companyFabName'),
    },
  },
  collectStatus: {
    key: 'collectStatus',
    title: <ColumnTitle>Collect/Convert/Insert</ColumnTitle>,
    dataIndex: 'collectStatus',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'collectStatus'),
    },
  },
  errorSummaryStatus: {
    key: 'errorSummaryStatus',
    title: <ColumnTitle>Send Error Summary</ColumnTitle>,
    dataIndex: 'errorSummaryStatus',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'errorSummaryStatus'),
    },
  },
  crasDataStatus: {
    key: 'crasDataStatus',
    title: <ColumnTitle>Create Cras Data</ColumnTitle>,
    dataIndex: 'crasDataStatus',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'crasDataStatus'),
    },
  },
  mpaVersionStatus: {
    key: 'mpaVersionStatus',
    title: <ColumnTitle>Version Check</ColumnTitle>,
    dataIndex: 'mpaVersionStatus',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'mpaVersionStatus'),
    },
  },
  stop: {
    key: 'stop',
    title: <ColumnTitle>Status</ColumnTitle>,
    dataIndex: 'stop',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'stop'),
    },
  },
  edit: {
    key: 'edit',
    title: <ColumnTitle>Edit</ColumnTitle>,
    dataIndex: 'jobId',
    align: 'center',
  },
  delete: {
    key: 'delete',
    title: <ColumnTitle>Delete</ColumnTitle>,
    dataIndex: 'jobId',
    align: 'center',
  },
};

const iconStyle = css`
  /* font-size: 1.25rem; */
  &:hover {
    color: ${blue[4]};
  }
  &:active {
    color: ${blue[6]};
  }
`;

const statusIconStyle = (isJob: boolean) => css`
  pointer-events: ${!isJob && 'none'};
  .running {
    color: #52c41a;
    -webkit-animation: blink 1s ease-in-out infinite alternate;
    animation: blink 1s ease-in-out infinite alternate;
    @keyframes blink {
      0% {
        opacity: 0;
      }
      100% {
        opacity: 1;
      }
    }
  }

  .stopped {
    color: #ff4d4f;
  }
  .text {
    cursor: ${isJob ? 'pointer' : 'default'};
    &:hover {
      color: ${isJob && blue[4]};
    }
    &:active {
      color: ${isJob && blue[6]};
    }
    margin-left: 0.3rem;
  }
`;
