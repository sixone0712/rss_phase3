import { DesktopOutlined, FileAddOutlined, InboxOutlined, ReloadOutlined } from '@ant-design/icons';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Button, message, Row, Select, Space, Upload } from 'antd';
import React, { useCallback } from 'react';
import useLocalJobSlices from '../../../hooks/useLocalJobSlices';
import useSiteName from '../../../hooks/useSiteName';
import useUploadFiles, { ResponseUploadFile } from '../../../hooks/useUploadFiles';
import { API_URL } from '../../../lib/constants';

export type LocalConfigureProps = {};

export default function LocalConfigure(): JSX.Element {
  const { disabledSelectSite, refreshSiteName, isFetching, data } = useSiteName('add', false);
  const { selectSite, setSelectSite } = useLocalJobSlices();
  const { uploadFiles, setUploadFiles, setResponseFiles } = useUploadFiles();
  const onChange = useCallback((info: any) => {
    const { fileList, file } = info;
    const { status } = file;

    if (status === 'uploading') {
      // do noting
    } else if (status === 'done') {
      message.success(`${info.file.name} file uploaded successfully.`);
    } else if (status === 'error') {
      message.error(`${info.file.name} file upload failed.`);
    } else if (status === 'removed') {
      // do noting
    }
    setUploadFiles(fileList);
    const fileIdList: ResponseUploadFile[] = fileList.map((item: any) => {
      return {
        name: item.name,
        fileIndex: item.response?.fileIndex,
        uid: item.uid,
        status: item.status,
      };
    });

    setResponseFiles(fileIdList);
  }, []);

  return (
    <>
      <SelectSiteName align="top">
        <Space css={spaceStyle}>
          <DesktopOutlined />
          <span>User-Fab Name</span>
        </Space>
        <Select
          showSearch
          labelInValue
          css={selectStyle}
          value={selectSite}
          placeholder="Select a site"
          onSelect={setSelectSite}
          loading={isFetching}
          optionFilterProp="children"
          filterOption={(input, option) => option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          disabled={disabledSelectSite}
        >
          {data?.map((item) => (
            <Select.Option key={item.siteId} value={item.siteId} label={item.crasCompanyFabName}>
              {item.crasCompanyFabName}
            </Select.Option>
          ))}
        </Select>
        <Button
          type="primary"
          icon={<ReloadOutlined />}
          css={btnStyle}
          onClick={refreshSiteName}
          loading={isFetching}
          disabled={isFetching}
        />
      </SelectSiteName>
      <FileUpload align="top">
        <Space css={spaceStyle}>
          <FileAddOutlined />
          <span>Files</span>
        </Space>
        <div css={uploadDraggerStyle}>
          <Upload.Dragger
            name="file"
            multiple
            maxCount={10}
            action={API_URL.UPLOAD_STATUS_LOCAL_JOB_FILE_URL}
            fileList={uploadFiles}
            onChange={onChange}
          >
            <div>
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">
                Click or drag file to this area to upload
                <br />
                (The maximum number of files to be uploaded is 10.)
              </p>
            </div>
          </Upload.Dragger>
        </div>
      </FileUpload>
    </>
  );
}

const SelectSiteName = styled(Row)`
  font-size: 1rem;
  flex-wrap: nowrap;
  /* height: 14.0625rem; */
`;
const FileUpload = styled(Row)`
  font-size: 1rem;
  margin-top: 6.25rem;
  flex-wrap: nowrap;
  /* height: 14.0625rem; */
`;

const spaceStyle = css`
  min-width: 13.25rem;
  /* font-size: 1.25rem; */
`;

const selectStyle = css`
  min-width: 33.75rem;
  text-align: center;
  font-size: inherit;
`;

const uploadDraggerStyle = css`
  .ant-upload .ant-upload-drag {
    width: 33.75rem;
  }
  .ant-upload-list.ant-upload-list-text {
    width: 33.75rem;
  }
`;

const btnStyle = css`
  border-radius: 0.625rem;
  margin-left: 0.5rem;
`;
