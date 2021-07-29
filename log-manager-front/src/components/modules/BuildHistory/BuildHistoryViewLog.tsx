import { css } from '@emotion/react';
import styled from '@emotion/styled';
import Space from 'antd/lib/space';
import React from 'react';
import { LazyLog } from 'react-lazylog';
import useBuildHistoryViewLog from '../../../hooks/useBuildHistoryViewLog';
import { BuildStatus } from '../../../types/status';
import { undraw_selection } from '../../../assets/images';

export type BuildHistoryViewLogProps = {};

export default function BuildHistoryViewLog({}: BuildHistoryViewLogProps): JSX.Element {
  const { requestUrl, status, name } = useBuildHistoryViewLog();

  if (!requestUrl) {
    return (
      <InitialScreen>
        <img alt="select a log history" src={undraw_selection} />
        <div className="text">Please select a log history.</div>
      </InitialScreen>
    );
  }

  return (
    <div css={logViewStyle(status)}>
      <div className="title-section">
        <Space>
          {status && <span className="status">●</span>}
          {name && <span className="name">{name}</span>}
        </Space>
      </div>
      {requestUrl && <LazyLog extraLines={1} enableSearch url={requestUrl} caseInsensitive />}
    </div>
  );
}

const InitialScreen = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-top: 0.25rem;
  margin-left: 1rem;
  width: 71.4375rem;
  height: 47.34375rem;
  .img {
    width: 25rem;
    height: auto;
  }
  .text {
    padding-top: 1rem;
    font-size: 2rem;
  }
`;

const logViewStyle = (status: BuildStatus | undefined) => css`
  margin-top: 0.25rem;
  margin-left: 1rem;
  width: 71.4375rem;
  height: 47.34375rem;
  .title-section {
    height: 1.875rem;
  }
  .status {
    font-size: 1rem;
    color: ${status && getColor(status)};
  }
  .name {
    font-size: 1rem;
  }
`;

function getColor(status: BuildStatus) {
  switch (status) {
    case 'success':
      return '#52c41a';
    case 'failure':
      return '#ff4d4f';
    case 'notbuild':
      return '#d9d9d9';
    case 'processing':
      return '#1890ff';
    case 'canceled':
      return '#faad14';
    default:
      return undefined;
  }
}
