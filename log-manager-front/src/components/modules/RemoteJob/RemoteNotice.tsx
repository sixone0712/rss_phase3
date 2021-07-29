import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Row } from 'antd';
import React from 'react';
import useRemoteJobSlices from '../../../hooks/useRemoteJobSlices';
import RemoteNoticeEmail from './RemoteNoticeEmail';
import RemoteNoticeSendTime from './RemoteNoticeSendTime';
export type RemoteNoticeProps = {};

export default function RemoteNotice(): JSX.Element {
  const {
    errorSummary,
    setPartialErrorSummary,
    crasData,
    setPartialCrasData,
    mpaVersion,
    setPartialMpaVersion,
  } = useRemoteJobSlices();

  return (
    <>
      <RemoteNoticeSendTime />
      <RemoteEmailSection>
        <RemoteNoticeEmail title="Error Summary" email={errorSummary} setEmail={setPartialErrorSummary} />
        <RemoteNoticeEmail title="Cras Data" email={crasData} setEmail={setPartialCrasData} />
        <RemoteNoticeEmail title="MPA Version" email={mpaVersion} setEmail={setPartialMpaVersion} />
      </RemoteEmailSection>
    </>
  );
}

export const remoteNoticetitleStyle = css`
  font-size: 1rem;
  min-width: 13.25rem;
`;

const RemoteEmailSection = styled(Row)`
  margin-bottom: 2rem;
`;
