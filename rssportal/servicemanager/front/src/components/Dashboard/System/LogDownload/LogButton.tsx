import React, { useRef } from 'react';
import { Button, Col, Row, Space } from 'antd';
import { DownloadOutlined, SyncOutlined } from '@ant-design/icons';
import { execFileDownload } from '../../../../api/download';
import { CancelInfo, LogFileList } from '../../../../typedef/LogDownloadType';

function LogButton({
  selected,
  fileList,
  selectedRowKeys,
  onRefresh,
  loading,
}: {
  selected: string | null;
  fileList: LogFileList;
  selectedRowKeys: React.Key[];
  onRefresh: () => void;
  loading: boolean;
}): JSX.Element {
  const cancelInfo = useRef<CancelInfo>({
    downloadId: null,
    cancel: false,
    isDownloading: false,
  });
  const onDownloadFile = () => {
    const selectedFileList: LogFileList = fileList.filter(list => {
      return selectedRowKeys.find(key => key === list.key) !== undefined;
    });
    console.log('selectedFileList', selectedFileList);
    if (selected) {
      execFileDownload(selected, selectedFileList, cancelInfo);
    }
  };

  return (
    <Row justify="space-between" align="middle" className="button-row">
      <Col className="button-col">{`${selectedRowKeys.length} Files Selected`}</Col>
      <Space>
        <Button
          type="primary"
          icon={<SyncOutlined />}
          onClick={onRefresh}
          disabled={loading}
        >
          Reload
        </Button>
        <Button
          icon={<DownloadOutlined />}
          onClick={onDownloadFile}
          disabled={selectedRowKeys.length <= 0}
          type="primary"
          danger
        >
          Download
        </Button>
      </Space>
    </Row>
  );
}

export default LogButton;
