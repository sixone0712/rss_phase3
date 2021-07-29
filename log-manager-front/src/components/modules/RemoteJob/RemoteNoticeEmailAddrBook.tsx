import { Modal, Transfer } from 'antd';
import { LabeledValue } from 'antd/lib/select';
import React from 'react';
import useRemoteJobAddrBook from '../../../hooks/useRemoteJobAddrBook';
import { AddressInfo } from '../../../lib/api/axios/types';
import { EmailOptionState, EmailOptionStateKey } from '../../../reducers/slices/remoteJob';

interface RemoteNoticeEmailAddrBookProps {
  type: 'Error Summary' | 'Cras Data' | 'MPA Version';
  visible: boolean;
  selectedTags: LabeledValue[];
  setEmail: (value: Partial<EmailOptionState>) => void;
}
export default function RemoteNoticeEmailAddrBook({
  type,
  visible,
  selectedTags,
  setEmail,
}: RemoteNoticeEmailAddrBookProps): JSX.Element {
  const {
    handleOk,
    handleCancel,
    addressList,
    targetKeys,
    handleChange,
    selectedKeys,
    handleSelectChange,
    isFetching,
  } = useRemoteJobAddrBook({ type, visible, selectedTags, setEmail });

  const filterOption = (inputValue: string, item: AddressInfo) =>
    item.name.indexOf(inputValue) > -1 || item.name.indexOf(inputValue) > -1;

  return (
    <Modal
      title={'Address Book'}
      visible={visible}
      onOk={handleOk}
      okButtonProps={{ disabled: isFetching }}
      onCancel={handleCancel}
      // cancelButtonProps={{
      //   disabled: isFetchingAddEdit,
      // }}
      // closable={!isFetchingAddEdit}
      // maskClosable={!isFetchingAddEdit}
      width={'1000px'}
    >
      <Transfer<AddressInfo>
        dataSource={isFetching ? [] : addressList}
        titles={['All Emails', 'Recipients']}
        showSearch
        listStyle={{
          width: 500,
          height: 500,
        }}
        targetKeys={isFetching ? [] : targetKeys}
        onChange={handleChange}
        render={(item) => (item.group ? item.name : `${item.name} <${item.email}>`)}
        filterOption={filterOption}
        disabled={isFetching}
        onSelectChange={handleSelectChange}
        selectedKeys={selectedKeys}
      />
    </Modal>
  );
}
