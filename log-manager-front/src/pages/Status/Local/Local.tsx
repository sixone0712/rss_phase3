import { css } from '@emotion/react';
import React from 'react';
import LocalJob from '../../../components/modules/LocalJob';
import LocalStatusTable from '../../../components/modules/LocalStatusTable';

export type LocalProps = {};

function Local({}: LocalProps) {
  return (
    <div css={localStatusTableStyle}>
      <LocalStatusTable />
    </div>
  );
}

type AddJobProps = {};

function AddJob({}: AddJobProps) {
  return (
    <div>
      <LocalJob />
    </div>
  );
}

Local.AddJob = AddJob;
export default Local;

const localStatusTableStyle = css`
  display: flex;
  justify-content: center;
  margin-top: 0.5rem;
`;
