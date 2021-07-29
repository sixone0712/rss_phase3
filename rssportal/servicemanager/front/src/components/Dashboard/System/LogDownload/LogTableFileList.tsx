import React, { useEffect, useState } from 'react';
import { Table } from 'antd';
import {
  LogFile,
  LogFileList,
  logFilter,
} from '../../../../typedef/LogDownloadType';
import LogButton from './LogButton';
import { TableRowSelection } from 'antd/es/table/interface';
import { FileListState } from '../../../../hooks/useAsyncAxios';
import {
  Key,
  SorterResult,
  TableAction,
  TablePaginationConfig,
} from 'antd/lib/table/interface';
import { bytesToSize } from '../../../../api/common';

function LogTableFileList({
  selected,
  fileList,
  onLoadFileList,
  listState,
}: {
  selected: string | null;
  fileList: LogFileList;
  onLoadFileList: () => void;
  listState: FileListState;
}): JSX.Element {
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [currentDataSource, setCurrentDataSource] = useState<LogFile[]>([]);
  const [filteredValue, setFilteredValue] = useState<React.Key[]>([]);

  const onRefresh = () => {
    setCurrentPage(1);
    setSelectedRowKeys([]);
    setCurrentDataSource([]);
    setFilteredValue([]);
    onLoadFileList();
  };

  useEffect(() => {
    if (selected) {
      onRefresh();
    }
  }, [selected]);

  const fileTypeFilterFunc = (
    value: string | number | boolean,
    record: LogFile,
  ) => typeof value === 'string' && record.fileType.indexOf(value) === 0;

  const onPageNation = (page: number, pageSize: number | undefined) => {
    setCurrentPage(page);
  };

  const onSelectChange = (
    selectedRowKeys: React.Key[],
    selectedRows: LogFileList,
  ) => {
    console.log('selectedRowKeys changed: ', selectedRowKeys);
    setSelectedRowKeys(selectedRowKeys);
  };

  const rowSelection: TableRowSelection<LogFile> = {
    selectedRowKeys,
    onChange: onSelectChange,
    onSelectAll: (selected, selectedRows, changeRows) => {
      console.log('onSelectAll', selected, selectedRows, changeRows);
      if (selected) {
        if (currentDataSource.length === 0) {
          setSelectedRowKeys(fileList.map(item => item.key));
        } else {
          setSelectedRowKeys(currentDataSource.map(item => item.key));
        }
      } else {
        setSelectedRowKeys([]);
      }
    },
  };

  const onRow = (record: LogFile, rowIndex: number | undefined) => {
    return {
      onClick: (event: React.MouseEvent<HTMLElement, MouseEvent>) => {
        if (record.key !== undefined) {
          let newselectedRowKeys;
          if (selectedRowKeys.find(item => item === record.key) !== undefined) {
            newselectedRowKeys = selectedRowKeys.filter(
              item => item !== record.key,
            );
          } else {
            newselectedRowKeys = selectedRowKeys.concat(record.key);
          }
          setSelectedRowKeys(newselectedRowKeys);
        }
      },
    };
  };
  const onTableChange = (
    pagination: TablePaginationConfig,
    filters: Record<string, Key[] | null>,
    sorter: SorterResult<LogFile> | SorterResult<LogFile>[],
    {
      currentDataSource,
      action,
    }: {
      currentDataSource: LogFile[];
      action: TableAction;
    },
  ) => {
    console.log('action', action);
    if (action === 'filter') {
      if (!filters.fileType) {
        setCurrentDataSource([]);
        setFilteredValue([]);
      } else {
        setCurrentDataSource(currentDataSource);
        setFilteredValue(filters.fileType);
      }
      setSelectedRowKeys([]);
    }
  };

  return (
    <div className="log-dl-table-list">
      <LogButton
        selected={selected}
        fileList={fileList}
        selectedRowKeys={selectedRowKeys}
        onRefresh={onRefresh}
        loading={listState.loading}
      />
      <Table<LogFile>
        rowKey={(record: LogFile) => record.key}
        tableLayout="fixed"
        size="small"
        bordered
        rowSelection={rowSelection}
        dataSource={fileList}
        pagination={{
          pageSize: 7,
          position: ['bottomCenter'],
          current: currentPage,
          defaultCurrent: 1,
          showSizeChanger: false,
          onChange: onPageNation,
        }}
        onChange={onTableChange}
        loading={listState.loading}
        onRow={onRow}
      >
        <Table.Column<LogFile>
          title={'File Type'}
          dataIndex={'fileType'}
          key={'fileType'}
          width={'35%'}
          align={'center'}
          filters={logFilter}
          onFilter={fileTypeFilterFunc}
          filteredValue={filteredValue}
          render={(value: any, record: LogFile, index: number) =>
            record.fileTypeName
          }
        />
        <Table.Column<LogFile>
          title={'File Name'}
          dataIndex={'fileName'}
          key={'fileName'}
          width={'50%'}
          align={'center'}
        />
        <Table.Column<LogFile>
          title={'File Size'}
          dataIndex={'fileSize'}
          key={'fileSize'}
          width={'15%'}
          align={'center'}
          render={(value: number) => bytesToSize(value)}
        />
      </Table>
    </div>
  );
}

export default LogTableFileList;
