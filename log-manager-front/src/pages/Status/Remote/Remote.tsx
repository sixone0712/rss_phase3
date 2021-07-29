import { css } from '@emotion/react';
import React from 'react';
import RemoteJob from '../../../components/modules/RemoteJob';
import RemoteStatusTable from '../../../components/modules/RemoteStatusTable';

export type RemoteJobType = 'add' | 'edit';

export type RemoteProps = {};

function Remote({}: RemoteProps) {
  return (
    <div css={remoteStatusTableStyle}>
      <RemoteStatusTable />
    </div>
  );
}

const remoteStatusTableStyle = css`
  display: flex;
  justify-content: center;
  margin-top: 0.5rem;
`;

type AddJobProps = {};

function AddJob({}: AddJobProps) {
  return (
    <div>
      <RemoteJob type={'add'} />
    </div>
  );
}

type EditJobProps = {};

function EditJob({}: EditJobProps) {
  return (
    <div>
      <RemoteJob type={'edit'} />
    </div>
  );
}
Remote.AddJob = AddJob;
Remote.EditJob = EditJob;
export default Remote;
