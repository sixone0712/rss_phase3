import React from 'react';
import { css } from '@emotion/react';
import { PresetStatusColorType } from 'antd/lib/_util/colors';
import { blue } from '@ant-design/colors';
import { Badge } from 'antd';
import { BuildStatus } from '../../../types/status';

export type StatusBadgeProps = {
  type: BuildStatus;
  onClick?: () => void;
};

const converStatusType = (
  type: BuildStatus
): { badgeStatus: PresetStatusColorType | undefined; textStatus: string } => {
  switch (type) {
    case 'success':
      return {
        badgeStatus: 'success',
        textStatus: 'Success',
      };

    case 'failure':
      return {
        badgeStatus: 'error',
        textStatus: 'failure',
      };
    case 'processing':
      return {
        badgeStatus: 'processing',
        textStatus: 'Processing',
      };
    case 'canceled':
      return {
        badgeStatus: 'warning',
        textStatus: 'Canceled',
      };
    case 'none':
      return {
        badgeStatus: undefined,
        textStatus: 'None',
      };
    default:
    case 'notbuild':
      return {
        badgeStatus: 'default',
        textStatus: 'Not Build',
      };
  }
};

export default function StatusBadge({ type, onClick }: StatusBadgeProps): JSX.Element {
  const { badgeStatus, textStatus } = converStatusType(type);

  return (
    <div css={containerStyle} onClick={onClick}>
      {badgeStatus ? (
        <Badge status={badgeStatus} text={textStatus} />
      ) : (
        <span className="ant-badge-status-text">{textStatus}</span>
      )}
    </div>
  );
}

const containerStyle = css`
  cursor: pointer;
  &:hover {
    .ant-badge-status-text {
      color: ${blue[4]};
    }
  }
  &:active {
    .ant-badge-status-text {
      color: ${blue[6]};
    }
  }
`;
