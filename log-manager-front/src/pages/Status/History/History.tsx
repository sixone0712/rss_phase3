import { css } from '@emotion/react';
import React from 'react';
import BuildHistory from '../../../components/modules/BuildHistory';
export type HistoryProps = {
  children?: React.ReactNode;
};

export default function History({ children }: HistoryProps) {
  return (
    <div css={style}>
      <BuildHistory />
    </div>
  );
}

const style = css`
  padding-left: 0.5rem;
  padding-right: 0.5rem;
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
`;

const getStatusTableColumnName = (column: string | null | undefined) => {
  switch (column) {
    case 'convert':
      return 'Status(Collect/Convert/Insert)';
    case 'error':
      return 'Send Error Summary';
    case 'cras':
      return 'Create Cras Data';
    case 'version':
      return 'Version Check';
    default:
      return '';
  }
};
