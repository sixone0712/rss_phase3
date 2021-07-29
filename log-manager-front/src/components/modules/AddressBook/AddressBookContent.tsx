import { blue } from '@ant-design/colors';
import { DeleteOutlined, EditOutlined, ReloadOutlined } from '@ant-design/icons';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Empty, Select, Spin, Table } from 'antd';
import { CompareFn } from 'antd/lib/table/interface';
import { AlignType, DataIndex } from 'rc-table/lib/interface';
import React, { Key, useCallback, useMemo } from 'react';
import useAddressBookContent from '../../../hooks/useAddressBookContent';
import { AddressInfo } from '../../../lib/api/axios/types';
import { compareTableItem } from '../../../lib/util/compareTableItem';
import TableHeader from '../TableHeader';

export type AddressBookContentProps = {};

export default function AddressBookContent({}: AddressBookContentProps): JSX.Element {
  const {
    addressList,
    isFetchingAddress,
    rowSelection,
    searchResult,
    handleChange,
    handleInputKeyDown,
    openEmailModal,
    debouncedhandleSearch,
    keyword,
    isSearching,
    selectedGroupName,
    searchedKeyword,
    openEmailDeleteModal,
    refreshAddressList,
  } = useAddressBookContent();
  const options = useMemo(
    () =>
      searchResult.map((result) => (
        <Select.Option value={result.id} key={result.id}>
          {result.group ? `@${result.name}` : `${result.name} <${result.email}>`}
        </Select.Option>
      )),
    [searchResult]
  );

  const titleRender = useCallback(
    () => (
      <TableHeader
        title={
          searchedKeyword
            ? `${selectedGroupName} (${addressList?.length ?? 0}) | ${searchedKeyword}`
            : `${selectedGroupName} | ${addressList?.length ?? 0}`
        }
        button1={{
          name: 'Delete',
          icon: <DeleteOutlined />,
          onClick: openEmailDeleteModal,
        }}
        button2={{
          icon: <ReloadOutlined />,
          onClick: refreshAddressList,
        }}
      />
    ),
    [selectedGroupName, addressList, openEmailDeleteModal]
  );

  const deleteRender = useCallback(
    (value: number, record: AddressInfo, index: number) => (
      <EditOutlined css={iconStyle} onClick={() => openEmailModal(record)} />
    ),
    [openEmailModal]
  );

  return (
    <div css={style}>
      <Select
        showSearch
        value={keyword}
        defaultActiveFirstOption={false}
        showArrow={false}
        filterOption={false}
        onSearch={debouncedhandleSearch}
        onChange={handleChange}
        notFoundContent={isSearching ? <Spin size="small" /> : keyword ? <Empty description="No Email" /> : null}
        onInputKeyDown={handleInputKeyDown}
        placeholder="Search Email Address"
        optionFilterProp="children"
        css={searchStyle}
      >
        {searchResult.length > 0 && options}
      </Select>
      <Table<AddressInfo>
        title={titleRender}
        rowKey={'id'}
        dataSource={isFetchingAddress ? undefined : addressList}
        bordered
        size="middle"
        pagination={{
          position: ['bottomCenter'],
          total: addressList?.length,
          showSizeChanger: true,
        }}
        rowSelection={rowSelection}
        loading={isFetchingAddress}
        css={tableStyle}
        tableLayout="fixed"
        locale={{
          emptyText: isFetchingAddress ? (
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Loading" />
          ) : (
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
          ),
        }}
      >
        <Table.Column<AddressInfo> {...addressColumProps.name} />
        <Table.Column<AddressInfo> {...addressColumProps.email} />
        <Table.Column<AddressInfo> {...addressColumProps.edit} width={100} render={deleteRender} />
      </Table>
    </div>
  );
}

const style = css`
  display: flex;
  flex-direction: column;
  margin-left: 1rem;
`;

const searchStyle = css`
  width: 30rem;
`;

const tableStyle = css`
  margin-top: 1rem;
  width: 65rem;
`;

const ColumnTitle = styled.div`
  font-weight: 700;
`;

export type AddressColumnName = 'name' | 'email' | 'edit';

export type AddressColumnPropsType = {
  [name in AddressColumnName]: {
    key?: Key;
    title?: React.ReactNode;
    dataIndex?: DataIndex;
    align?: AlignType;
    sorter?:
      | boolean
      | CompareFn<AddressInfo>
      | {
          compare?: CompareFn<AddressInfo>;
          /** Config multiple sorter order priority */
          multiple?: number;
        };
  };
};

const addressColumProps: AddressColumnPropsType = {
  name: {
    key: 'name',
    title: <ColumnTitle>Name</ColumnTitle>,
    dataIndex: 'name',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'name'),
    },
  },

  email: {
    key: 'email',
    title: <ColumnTitle>Email</ColumnTitle>,
    dataIndex: 'email',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'email'),
    },
  },

  edit: {
    key: 'edit',
    title: <ColumnTitle>Edit</ColumnTitle>,
    dataIndex: 'jobId',
    align: 'center',
  },
};

const iconStyle = css`
  /* font-size: 1.25rem; */
  &:hover {
    color: ${blue[4]};
  }
  &:active {
    color: ${blue[6]};
  }
`;
