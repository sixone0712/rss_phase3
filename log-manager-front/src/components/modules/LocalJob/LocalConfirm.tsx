import { DesktopOutlined, FileAddOutlined, PaperClipOutlined } from '@ant-design/icons';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Col, Row, Space } from 'antd';
import React, { useMemo } from 'react';
import useLocalJobSlices from '../../../hooks/useLocalJobSlices';
import useUploadFiles from '../../../hooks/useUploadFiles';

export type LocalConfirmProps = {};
export default function LocalConfirm(): JSX.Element {
  const { selectSite } = useLocalJobSlices();
  const { responseFiles } = useUploadFiles();

  const doneFileslen = useMemo(() => responseFiles.filter((item) => item.status === 'done').length, [responseFiles]);

  return (
    <>
      <SiteName align="middle">
        <Space css={spaceStyle}>
          <DesktopOutlined />
          <span>User-Fab Name</span>
        </Space>
        <SelectedSite>{selectSite?.label}</SelectedSite>
      </SiteName>
      <FileUpload align="top">
        <Space css={spaceStyle}>
          <FileAddOutlined />
          <span>Files</span>
        </Space>
        <UploadFiles>
          <UploadFileCount>{doneFileslen} Files</UploadFileCount>
          {responseFiles.map((item) => (
            <UploadFileList key={item.uid}>
              <div css={loadFileStyle(item.status)}>
                <PaperClipOutlined className="icon" />
                <span title={item.name} className="text">{`${item.name} ${
                  item.status === 'error' ? '(Error)' : ''
                }`}</span>
              </div>
            </UploadFileList>
          ))}
        </UploadFiles>
      </FileUpload>
    </>
  );
}

const SiteName = styled(Row)`
  font-size: 1rem;
  flex-wrap: nowrap;
`;
const FileUpload = styled(Row)`
  font-size: 1rem;
  margin-top: 2rem;
  flex-wrap: nowrap;
`;

const SelectedSite = styled(Col)``;
const UploadFiles = styled(Col)``;
const UploadFileCount = styled(Row)``;
const UploadFileList = styled(Row)`
  margin-left: 0.5rem;
`;

const spaceStyle = css`
  min-width: 13.25rem;
`;

const loadFileStyle = (status: string | undefined) => css`
  .icon {
    margin-right: 0.5rem;
    color: rgba(0, 0, 0, 0.45);
  }
  .text {
    color: ${status === 'error' && 'red'};
    text-decoration-line: ${status === 'error' && 'line-through'};
    width: 49rem;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: block;
  }
  display: flex;
  align-items: center;
`;
