import { LabeledValue } from 'antd/lib/select';
import { TransferDirection } from 'antd/lib/transfer';
import { AxiosError } from 'axios';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useQuery } from 'react-query';
import { getAddressGroupEmailList } from '../lib/api/axios/requests';
import { AddressInfo } from '../lib/api/axios/types';
import { QUERY_KEY } from '../lib/api/query/queryKey';
import { openNotification } from '../lib/util/notification';
import { EmailOptionState } from '../reducers/slices/remoteJob';
import { TransferAddressInfo } from './useAddressBookAddEditGroup';

interface useRemoteJobAddrBookProps {
  type: 'Error Summary' | 'Cras Data' | 'MPA Version';
  visible: boolean;
  selectedTags: LabeledValue[];
  setEmail: (value: Partial<EmailOptionState>) => void;
}

export default function useRemoteJobAddrBook({ type, visible, selectedTags, setEmail }: useRemoteJobAddrBookProps) {
  const [targetKeys, setTargetKeys] = useState<string[] | undefined>(undefined);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);
  const [customEmails, setCustomEmails] = useState<LabeledValue[]>([]);

  const setVisible = useCallback(
    (value: boolean) => {
      setEmail({ showAddrBook: value });
    },
    [setEmail]
  );

  const setSelectedTags = useCallback(
    (value: LabeledValue[]) => {
      setEmail({ selectedTags: value });
    },
    [setEmail]
  );

  const { data, isFetching } = useQuery<AddressInfo[], AxiosError>(
    [QUERY_KEY.ADDRESS_GET_GROUPS_EMAILS],
    () => getAddressGroupEmailList(),
    {
      initialData: [],
      placeholderData: [],
      enabled: visible,
      refetchOnWindowFocus: false,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to response the list of group and email!`, error);
      },
    }
  );

  const addressList: TransferAddressInfo[] = useMemo(
    () =>
      data?.map((item: AddressInfo) => {
        const { id, name, group } = item;
        return {
          key: id.toString(),
          ...item,
          name: group ? `@${name}` : name,
        };
      }) ?? [],
    [data]
  );

  const handleOk = useCallback(() => {
    const groupTags: LabeledValue[] = [];
    const emailTags: LabeledValue[] = [];
    targetKeys?.map((item) => {
      const foundItem = addressList.find((innerItem) => innerItem.id === +item);
      if (foundItem) {
        if (foundItem.group) {
          const value = `group_${foundItem.id}`;
          groupTags.push({ key: value, value, label: foundItem.name });
        } else {
          const value = `email_${foundItem.id}`;
          emailTags.push({ key: value, value, label: `${foundItem?.name} <${foundItem?.email}>` });
        }
      }
    });

    setSelectedTags([...groupTags, ...emailTags, ...customEmails]);
    setVisible(false);
  }, [addressList, targetKeys, setSelectedTags, setVisible]);

  const handleCancel = useCallback(() => {
    setVisible(false);
  }, [setVisible]);

  const handleChange = useCallback(
    (targetKeys: string[], direction: TransferDirection, moveKeys: string[]) => {
      setTargetKeys(targetKeys);
    },
    [setTargetKeys]
  );

  const handleSelectChange = useCallback(
    (sourceSelectedKeys: string[], targetSelectedKeys: string[]) => {
      setSelectedKeys([...sourceSelectedKeys, ...targetSelectedKeys]);
    },
    [setSelectedKeys]
  );

  useEffect(() => {
    if (visible) {
      const groups = selectedTags
        .filter((item) => item.value.toString().startsWith('group_'))
        .map((item) => item.value.toString().replace('group_', ''));

      const emails = selectedTags
        .filter((item) => item.value.toString().startsWith('email_'))
        .map((item) => item.value.toString().replace('email_', ''));

      const custom = selectedTags.filter(
        (item) => !item.value.toString().startsWith('group_') && !item.value.toString().startsWith('email_')
      );

      setTargetKeys([...groups, ...emails]);
      setCustomEmails(custom);
    } else {
      setTargetKeys([]);
      setCustomEmails([]);
    }
  }, [visible]);

  return {
    handleOk,
    handleCancel,
    addressList,
    targetKeys,
    setTargetKeys,
    handleChange,
    isFetching,
    selectedKeys,
    handleSelectChange,
    visible,
    setVisible,
  };
}
