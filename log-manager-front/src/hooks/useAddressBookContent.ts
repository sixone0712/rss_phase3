import { Modal } from 'antd';
import { TableRowSelection } from 'antd/lib/table/interface';
import { AxiosError } from 'axios';
import React, { Key, KeyboardEventHandler, useCallback, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from 'react-query';
import { useDispatch, useSelector } from 'react-redux';
import { useDebouncedCallback } from 'use-debounce';
import { deleteAddressEmail, getAddressEmailList, searchAddressEmail } from '../lib/api/axios/requests';
import { AddressInfo } from '../lib/api/axios/types';
import { MUTATION_KEY } from '../lib/api/query/mutationKey';
import { QUERY_KEY } from '../lib/api/query/queryKey';
import { openNotification } from '../lib/util/notification';
import {
  AddressGroupSelector,
  DEFAULT_ALL_ADDRESS_KEY,
  DEFAULT_ALL_ADDRESS_NAME,
  DEFAULT_SEARCHED_NAME,
  setEditEmailReducer,
  setSelectGorupReducer,
  setVisibleEmailModalReducer,
} from '../reducers/slices/address';

export default function useAddressBookContent() {
  const { id: selectedGroupId, name: selectedGroupName, keyword: searchedKeyword } = useSelector(AddressGroupSelector);
  const [searchResult, setSearchResult] = useState<AddressInfo[]>([]);
  const [keyword, setKeyword] = useState<string | undefined>(undefined);
  const queryClient = useQueryClient();
  const dispatch = useDispatch();
  const [selectEmail, setSelectEmail] = useState<Key[]>([]);
  const setVisibleEmailModal = useCallback(
    (visible: boolean) => {
      dispatch(setVisibleEmailModalReducer(visible));
    },
    [dispatch]
  );
  const debouncedhandleSearch = useDebouncedCallback((value: string) => handleSearch(value), 400);

  const { data: addressList, isFetching: isFetchingAddress } = useQuery<AddressInfo[], AxiosError>(
    [QUERY_KEY.ADDRESS_GET_EMAILS, selectedGroupId],
    () => getAddressEmailList(selectedGroupId),
    {
      initialData: [],
      enabled: selectedGroupId && selectedGroupName !== DEFAULT_SEARCHED_NAME ? true : false,
      refetchOnWindowFocus: false,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to response the list of address`, error);
      },
    }
  );

  const { mutate: mutateSearching, isLoading: isSearching } = useMutation(
    (reqData: string) => searchAddressEmail(reqData),
    {
      mutationKey: MUTATION_KEY.ADDRESS_SEARCH_EMAIL,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to search email address`, error);
      },
      onSuccess: (result: AddressInfo[]) => {
        setSearchResult(result);
      },
    }
  );

  const { mutateAsync: mutateAsyncDeleteEmail } = useMutation((emailIds: number[]) => deleteAddressEmail(emailIds), {
    mutationKey: MUTATION_KEY.ADDRESS_DELETE_EMAIL,
  });

  const rowSelection: TableRowSelection<AddressInfo> = {
    selectedRowKeys: selectEmail,
    onChange: (selectedRowKeys: React.Key[], selectedRows: AddressInfo[]) => {
      console.log(`selectedRowKeys: ${selectedRowKeys}`, 'selectedRows: ', selectedRows);
      setSelectEmail(selectedRowKeys);
    },
    hideSelectAll: true,
  };

  const handleSearch = async (value: string) => {
    if (value) {
      mutateSearching(value);
      setKeyword(value);
    } else {
      setSearchResult([]);
      setKeyword(undefined);
    }
  };

  // select search item
  const handleChange = (value: Key) => {
    queryClient.setQueriesData(
      QUERY_KEY.ADDRESS_GET_EMAILS,
      searchResult.filter((item) => item.id === +value)
    );
    setKeyword(undefined);
  };

  const handleInputKeyDown: KeyboardEventHandler<HTMLInputElement> = (event) => {
    if (event.key === 'Enter' && !isSearching) {
      queryClient.invalidateQueries(QUERY_KEY.ADDRESS_GET_EMAILS);
      dispatch(setSelectGorupReducer({ id: DEFAULT_ALL_ADDRESS_KEY, name: DEFAULT_SEARCHED_NAME, keyword: keyword }));
      queryClient.setQueriesData([QUERY_KEY.ADDRESS_GET_EMAILS, DEFAULT_ALL_ADDRESS_KEY], searchResult);
      setKeyword(undefined);
      setSearchResult([]);
    }
  };

  const openEmailModal = useCallback(
    (editEmail?: AddressInfo | undefined) => {
      setVisibleEmailModal(true);
      dispatch(setEditEmailReducer(editEmail));
    },
    [dispatch]
  );

  const refreshGroupEmailList = () => {
    queryClient.prefetchQuery(QUERY_KEY.ADDRESS_GET_GROUPS);
    queryClient.prefetchQuery([QUERY_KEY.ADDRESS_GET_EMAILS, DEFAULT_ALL_ADDRESS_KEY]);
    dispatch(setSelectGorupReducer({ id: DEFAULT_ALL_ADDRESS_KEY, name: DEFAULT_ALL_ADDRESS_NAME }));
  };

  const refreshAddressList = () => {
    queryClient.prefetchQuery([QUERY_KEY.ADDRESS_GET_EMAILS, selectedGroupId]);
  };

  const openEmailDeleteModal = useCallback(() => {
    const confirm = Modal.confirm({
      className: 'delete_email',
      title: 'Delete Email',
      content: `Are you sure to delete seleted emails'?`,
      onOk: async () => {
        diableCancelBtn();
        try {
          await mutateAsyncDeleteEmail(selectEmail.map((item) => +item));
          openNotification('success', 'Success', `Succeed to delete emails.`);
        } catch (e) {
          console.error(e);
          openNotification('error', 'Error', `Failed to delete emails!`, e);
        } finally {
          refreshGroupEmailList();
        }
      },
    });

    const diableCancelBtn = () => {
      confirm.update({
        cancelButtonProps: {
          disabled: true,
        },
      });
    };
  }, [mutateAsyncDeleteEmail, refreshGroupEmailList, selectEmail]);

  return {
    addressList,
    isFetchingAddress,
    rowSelection,
    searchResult,
    handleChange,
    handleInputKeyDown,
    openEmailModal,
    keyword,
    debouncedhandleSearch,
    isSearching,
    selectedGroupId,
    selectedGroupName,
    searchedKeyword,
    openEmailDeleteModal,
    selectEmail,
    refreshAddressList,
  };
}
