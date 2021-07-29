import { blue } from '@ant-design/colors';
import { CheckOutlined, DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Empty, Table } from 'antd';
import { CompareFn } from 'antd/lib/table/interface';
import { AlignType, DataIndex } from 'rc-table/lib/interface';
import React, { Key, useCallback } from 'react';
import useCrasDataEdit from '../../../hooks/useCrasDataEdit';
import { compareTableItem } from '../../../lib/util/compareTableItem';
import { CrasDataManualInfo } from '../../../types/crasData';
import CustomIcon from '../../atoms/CustomIcon';
import TableHeader from '../TableHeader';
import CrasDataEditCreateDrawer from './CrasDataEditCreateDrawer';
import CrasDataEditJudgeDrawer from './CrasDataEditJudgeDrawer';
export type CrasDataEditCreateProps = {
  type: 'create' | 'judge';
};

export default function CrasDataEdit({ type }: CrasDataEditCreateProps): JSX.Element {
  const {
    manualList,
    isFetchingList,
    openDeleteModal,
    openEditModal,
    openAddModal,
    refreshList,
    goBack,
    siteName,
  } = useCrasDataEdit({ type });

  const titleRender = useCallback(
    () => (
      <TableHeader
        title={`Registered Item : ${manualList?.length ?? 0}`}
        button1={{
          name: 'Add',
          icon: <PlusOutlined />,
          onClick: openAddModal,
        }}
        button2={{
          icon: <ReloadOutlined />,
          onClick: refreshList,
        }}
      />
    ),
    [manualList, openAddModal, refreshList, goBack]
  );

  const numberRender = useCallback((value: number, record: CrasDataManualInfo, index: number) => value + 1, []);

  const enableRender = useCallback(
    (value: boolean, record: CrasDataManualInfo, index: number) => (value ? <CheckOutlined /> : ''),
    []
  );

  const editRenter = useCallback(
    (value: number, record: CrasDataManualInfo, index: number) => {
      const onClick = () => openEditModal(value);
      return <EditOutlined css={iconStyle} onClick={onClick} />;
    },
    [openEditModal]
  );

  const deleteRender = useCallback(
    (value: number, record: CrasDataManualInfo, index: number) => {
      const onClick = () => openDeleteModal(value);
      return <DeleteOutlined css={iconStyle} onClick={onClick} />;
    },
    [openDeleteModal]
  );

  return (
    <div css={style}>
      <div className="title">
        <CustomIcon className="go-back" name="go_back" onClick={goBack} />
        <span className="name">
          {type === 'create'
            ? `Edit Create Cras Data Item (${siteName})`
            : `Edit Cras Data Judge Rules Item (${siteName})`}
        </span>
      </div>
      <Table<CrasDataManualInfo>
        rowKey={'siteId'}
        dataSource={isFetchingList ? undefined : manualList}
        bordered
        title={titleRender}
        size="middle"
        pagination={{
          position: ['bottomCenter'],
          total: manualList?.length ?? 0,
          showSizeChanger: true,
        }}
        loading={isFetchingList}
        css={tableStyle}
        locale={{
          emptyText: isFetchingList ? <Empty description="Loading" /> : <Empty description="No Data" />,
        }}
      >
        <Table.Column<CrasDataManualInfo> {...createCrasColumnProps.index} render={numberRender} />
        <Table.Column<CrasDataManualInfo> {...createCrasColumnProps.itemName} />
        <Table.Column<CrasDataManualInfo> {...createCrasColumnProps.enable} render={enableRender} />
        <Table.Column<CrasDataManualInfo> {...createCrasColumnProps.edit} render={editRenter} />
        <Table.Column<CrasDataManualInfo> {...createCrasColumnProps.delete} render={deleteRender} />
      </Table>
      <CrasDataEditCreateDrawer />
      <CrasDataEditJudgeDrawer />
    </div>
  );
}

const style = css`
  .title {
    display: flex;
    align-items: center;
    font-size: 1.5rem;
    margin-bottom: 0.5rem;
    .name {
      margin-left: 1rem;
    }
    .go-back {
      &:hover {
        color: ${blue[4]};
      }
      &:active {
        color: ${blue[6]};
      }
    }
  }
`;

const tableStyle = css`
  width: 86rem;
`;

const iconStyle = css`
  font-size: 1rem;
  &:hover {
    color: ${blue[4]};
  }
  &:active {
    color: ${blue[6]};
  }
`;

const CreateCrasColumnTitle = styled.div`
  font-weight: 700;
`;

type CreateCrasColumnName = 'index' | 'itemName' | 'enable' | 'edit' | 'delete';

type CreateCrasColumnPropsType = {
  [name in CreateCrasColumnName]: {
    key?: Key;
    title?: React.ReactNode;
    dataIndex?: DataIndex;
    align?: AlignType;
    sorter?:
      | boolean
      | CompareFn<CrasDataManualInfo>
      | {
          compare?: CompareFn<CrasDataManualInfo>;
          /** Config multiple sorter order priority */
          multiple?: number;
        };
  };
};

const createCrasColumnProps: CreateCrasColumnPropsType = {
  index: {
    key: 'index',
    title: <CreateCrasColumnTitle>No</CreateCrasColumnTitle>,
    dataIndex: 'index',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'index'),
    },
  },
  itemName: {
    key: 'itemName',
    title: <CreateCrasColumnTitle>Item Name</CreateCrasColumnTitle>,
    dataIndex: 'itemName',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'itemName'),
    },
  },

  enable: {
    key: 'enable',
    title: <CreateCrasColumnTitle>Enable</CreateCrasColumnTitle>,
    dataIndex: 'enable',
    align: 'center',
    sorter: {
      compare: (a, b) => compareTableItem(a, b, 'itemName'),
    },
  },
  edit: {
    key: 'edit',
    title: <CreateCrasColumnTitle>Edit</CreateCrasColumnTitle>,
    dataIndex: 'itemId',
    align: 'center',
  },
  delete: {
    key: 'delete',
    title: <CreateCrasColumnTitle>Delete</CreateCrasColumnTitle>,
    dataIndex: 'itemId',
    align: 'center',
  },
};
