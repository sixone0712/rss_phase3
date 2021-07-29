import { blue } from '@ant-design/colors';
import { css } from '@emotion/react';
import { Empty, Menu, Pagination, Spin } from 'antd';
import Sider from 'antd/lib/layout/Sider';
import React, { useCallback } from 'react';
import useBuildHistoryMenu from '../../../hooks/useBuildHistoryMenu';
import { LOG_HISTORY_MAX_LIST_COUNT } from '../../../lib/constants';
import { BuildHistorySelectedLogState } from '../../../reducers/slices/buildHistory';
import StatusBadge from '../../atoms/StatusBadge';
export type BuildHistoryMenuProps = {};

export default function BuildHistoryMenu({}: BuildHistoryMenuProps): JSX.Element {
  const {
    isFetching,
    selectedLog,
    setSelectedLog,
    totalHistoryListLen,
    historyList,
    currentPage,
    onChangeCurrentPage,
  } = useBuildHistoryMenu();

  const onSelectHistory = useCallback(
    ({ id, status, name }: BuildHistorySelectedLogState) => {
      setSelectedLog({ id, status, name });
    },
    [setSelectedLog]
  );

  return (
    <Sider theme="light" width={200} css={siderStyle}>
      {isFetching ? (
        <div css={spinStyle}>
          <Spin />
        </div>
      ) : (
        <>
          {totalHistoryListLen > 0 ? (
            <Menu mode="inline" className="Sider-Menu" defaultSelectedKeys={[`${selectedLog?.id}`]}>
              {historyList &&
                historyList?.length > 0 &&
                historyList.map((item) => {
                  const { id, status, name } = item;
                  return (
                    <Menu.Item key={id} css={menuItemStyle} onClick={() => onSelectHistory({ id, status, name })}>
                      <StatusBadge type={status} />
                      <div className="name">{name}</div>
                    </Menu.Item>
                  );
                })}
            </Menu>
          ) : (
            <div css={emptyDataStyle}>
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
            </div>
          )}
          <div css={pageNationStyle}>
            <Pagination
              defaultCurrent={1}
              total={totalHistoryListLen}
              current={currentPage}
              pageSize={LOG_HISTORY_MAX_LIST_COUNT}
              simple
              hideOnSinglePage
              size={'small'}
              onChange={onChangeCurrentPage}
            />
          </div>
        </>
      )}
    </Sider>
  );
}

const spinStyle = css`
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 1rem;
  border-right: 1px solid #f0f0f0;
  height: 49.78125rem;
`;

const siderStyle = css`
  height: 100%;
  .Sider-Menu {
    height: 49.78125rem;
    overflow: auto;
    border-right: 1px solid #f0f0f0;
  }
`;

const menuItemStyle = css`
  line-height: 30px !important;
  height: 60px !important;
  width: 100% !important;

  .name {
    font-size: 0.8rem;
  }

  &:hover {
    .ant-badge-status-text,
    .name {
      color: ${blue[4]};
    }
  }
  &:active {
    .ant-badge-status-text,
    .name {
      color: ${blue[6]};
    }
  }
`;

const emptyDataStyle = css`
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #f0f0f0;
  height: 49.78125rem;
`;

const pageNationStyle = css`
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 0.5rem;
  margin-bottom: 0.5rem;
`;
