import React from 'react';
import { notification } from 'antd';
import dayjs from 'dayjs';
import styled from '@emotion/styled';
import { IconType } from 'antd/lib/notification';
import { css } from '@emotion/react';
import { AxiosError } from 'axios';

const DivDate = styled.div`
  text-align: right;
  font-size: 13px;
  color: gray;
`;

export interface OpenNotification {
  (
    type: IconType,
    message: string,
    description: React.ReactNode,
    // errorMsg?: string,
    // status?: string | number,
    // statusText?: string
    error?: AxiosError
  ): void;
}

export const openNotification: OpenNotification = (type, message, description, error) => {
  // if (type === 'error') {
  //   notification.destroy();
  // }

  const status = error?.response?.status;
  const statusText = error?.response?.statusText;
  const reason = error?.response?.data.message;

  notification[type]({
    message: message,
    description: (
      <>
        <div css={style}>
          {Array.isArray(description) ? description.map((item, idx) => <div key={idx}>{item}</div>) : description}
          {reason && (
            <>
              <p />
              <div>{`Reason : ${reason}`}</div>
            </>
          )}
          {status && statusText && <div>{`Status : ${statusText}(${status})`}</div>}
        </div>
        <p />
        <DivDate>{dayjs().add(1, 'day').format('YYYY-MM-DD HH:mm:ss')}</DivDate>
      </>
    ),
    duration: type === 'error' ? 0 : 4.5,
  });
};

const style = css`
  display: flex;
  flex-direction: column;
`;
