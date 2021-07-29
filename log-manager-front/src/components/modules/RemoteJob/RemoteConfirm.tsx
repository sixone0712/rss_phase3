import { ClockCircleOutlined, DesktopOutlined, NotificationOutlined, ProfileOutlined } from '@ant-design/icons';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Col, Row, Space } from 'antd';
import React from 'react';
import useRemoteJobSlices from '../../../hooks/useRemoteJobSlices';
import EmailSettingView from '../../atoms/EmailSettingView';
export type RemoteConfirmProps = {};

export default function RemoteConfirm({}: RemoteConfirmProps): JSX.Element {
  const { selectSite, selectPlans, sendingTimes, errorSummary, crasData, mpaVersion } = useRemoteJobSlices();

  return (
    <>
      <SiteName align="top">
        <Space css={spaceStyle}>
          <DesktopOutlined />
          <span>User-Fab Name</span>
        </Space>
        <SelectedSite>{selectSite?.label}</SelectedSite>
      </SiteName>
      <Plans align="top">
        <Space css={spaceStyle}>
          <ProfileOutlined />
          <span>Select Plans</span>
        </Space>
        <SelectedPlans>{`${selectPlans.length} Plans`}</SelectedPlans>
      </Plans>
      <Notice align="top">
        <Space css={spaceStyle}>
          <NotificationOutlined />
          <span>Notice</span>
        </Space>
        <NoticeSettings>
          {sendingTimes.length > 0 ? (
            <>
              <DailySendingTime sendingTimes={sendingTimes} />
              {errorSummary.enable && (
                <EmailSettingView
                  title={'Error Summary'}
                  recipients={errorSummary.selectedTags}
                  before={errorSummary.before}
                />
              )}
              {crasData.enable && (
                <EmailSettingView title={'Cras Data'} recipients={crasData.selectedTags} before={crasData.before} />
              )}
              {mpaVersion.enable && (
                <EmailSettingView
                  title={'MPA Version'}
                  recipients={mpaVersion.selectedTags}
                  before={mpaVersion.before}
                />
              )}
            </>
          ) : (
            'None'
          )}
        </NoticeSettings>
      </Notice>
    </>
  );
}

const noticeContentsStyle = css`
  display: flex;
  flex-direction: row;
  .title {
    display: flex;
    align-items: flex-start;
    .text {
      margin-left: 0.5rem;
    }
    width: 10.625rem;
  }

  .value {
    margin-left: 2rem;
    width: 37.5rem;
  }
`;

const SiteName = styled(Row)`
  font-size: 1rem;
  flex-wrap: nowrap;
`;

const Plans = styled(Row)`
  margin-top: 2rem;
  font-size: 1rem;
  flex-wrap: nowrap;
`;

const Notice = styled(Row)`
  margin-top: 2rem;
  font-size: 1rem;
  flex-wrap: nowrap;
`;

const SelectedSite = styled(Col)``;
const SelectedPlans = styled(Col)``;
const NoticeSettings = styled(Row)`
  display: flex;
  flex-direction: column;
`;

const spaceStyle = css`
  min-width: 13.25rem;
`;

interface DailySendingTimeProps {
  sendingTimes: string[];
}
function DailySendingTime({ sendingTimes }: DailySendingTimeProps) {
  return (
    <div css={noticeContentsStyle}>
      <div className="title">
        <div className="image">
          <ClockCircleOutlined />
        </div>
        <div className="text">Daily Sending Time</div>
      </div>

      <div className="value">{sendingTimes.join(', ')}</div>
    </div>
  );
}
