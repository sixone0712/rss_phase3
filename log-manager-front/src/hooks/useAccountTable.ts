import { AxiosError } from 'axios';
import { useCallback, useState } from 'react';
import { useQuery, useQueryClient } from 'react-query';
import { getUsers } from '../lib/api/axios/requests';
import { UserInfo } from '../lib/api/axios/types';
import { QUERY_KEY } from '../lib/api/query/queryKey';
import { openNotification } from '../lib/util/notification';
import { initialRolesState, UserRolesBoolean } from '../reducers/slices/loginUser';

export default function useAccountTable() {
  const [visiblePermission, setVisiblePermission] = useState(false);
  const [visibleAddUser, setVisibleAddUser] = useState(false);
  const [id, setId] = useState(0);
  const [rules, setRules] = useState<UserRolesBoolean>({
    ...initialRolesState,
  });
  const { data: users, isFetching: isFetchingUsers } = useQuery<UserInfo[], AxiosError>(
    QUERY_KEY.ACCOUNT_GET_USERS,
    getUsers,
    {
      initialData: [],
      refetchOnWindowFocus: false,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to response the list of user account`, error);
      },
    }
  );
  const queryClient = useQueryClient();
  const usersLen = users?.length ?? 0;

  const refreshUsers = useCallback(() => {
    queryClient.prefetchQuery(QUERY_KEY.ACCOUNT_GET_USERS);
  }, [queryClient]);

  return {
    users,
    isFetchingUsers,
    refreshUsers,
    usersLen,
    visiblePermission,
    setVisiblePermission,
    visibleAddUser,
    setVisibleAddUser,
    rules,
    setRules,
    id,
    setId,
  };
}
