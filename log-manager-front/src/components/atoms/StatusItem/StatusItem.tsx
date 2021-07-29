import React from 'react';
import { css } from '@emotion/react';
import { Space } from 'antd';
import CustomIcon from '../CustomIcon';
import { green, grey, red, presetPalettes, blue } from '@ant-design/colors';
import { BuildStatus } from '../../../types/status';

export type StatusItemProps = {
  status: BuildStatus;
  onClick?: () => void;
};

export default function StatusItem({ status, onClick }: StatusItemProps): JSX.Element {
  return (
    <div onClick={onClick}>
      <Space>
        <CustomIcon name="circle" css={statusIconStyle(status)} />
        <span css={statusTextStyle}>{status}</span>
      </Space>
    </div>
  );
}

const statusIconStyle = (status: BuildStatus) => {
  let color = grey[4];
  if (status === 'success') color = green[5];
  else if (status === 'failure') color = red[4];
  return css`
    color: ${color};
  `;
};

const statusTextStyle = css`
  text-decoration: underline;
  &:hover {
    color: ${blue[4]};
  }
  &:active {
    color: ${blue[6]};
  }
`;
