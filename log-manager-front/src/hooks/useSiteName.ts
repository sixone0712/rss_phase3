import { useCallback, useMemo } from 'react';
import { useQuery, useQueryClient } from 'react-query';
import { useSelector } from 'react-redux';
import { getConfigureSitesNames, getConfigureSitesNamesNotAdded } from '../lib/api/axios/requests';
import { ResGetSiteName } from '../lib/api/axios/types';
import { openNotification } from '../lib/util/notification';
import { RemoteJobType } from '../pages/Status/Remote/Remote';
import { remoteJobSiteSelector } from '../reducers/slices/remoteJob';

// export default function useSiteName(type: RemoteJobType) {
//   const { isLoading, isError, data, error, status, isFetching } = useQuery<ResGetSiteName[]>(
//     'get_site_names_not_added',
//     getConfigureSitesNamesNotAdded,
//     {
//       refetchOnWindowFocus: false,
//       // refetchOnMount: false,
//       // refetchOnMount: true,
//       // initialData: [],
//       enabled: type === 'add',
//       onError: () => {
//         queryClient.setQueryData('get_site_names_not_added', []);
//         openNotification('error', 'Error', `Failed to get site name.`);
//       },
//     }
//   );
//   const selectSite = useSelector(remoteJobSiteSelector);
//   const queryClient = useQueryClient();

//   const refreshSiteName = useCallback(() => {
//     queryClient.fetchQuery('get_site_names_not_added');
//   }, [queryClient]);

//   const disabledSelectSite = useMemo(() => {
//     if (isFetching) {
//       return true;
//     } else {
//       if (type === 'edit') {
//         return !!selectSite;
//       } else {
//         return false;
//       }
//     }
//   }, [selectSite, type, isFetching]);

//   return {
//     disabledSelectSite,
//     refreshSiteName,
//     isFetching,
//     data,
//   };
// }

export default function useSiteName(type: RemoteJobType, notAdded = false) {
  const { data, isFetching } = useQuery<ResGetSiteName[]>('get_site_names', getConfigureSitesNames, {
    refetchOnWindowFocus: false,
    // refetchOnMount: false,
    // refetchOnMount: true,
    // initialData: [],
    enabled: type === 'add' && !notAdded,
    onError: () => {
      queryClient.setQueryData('get_site_names', []);
      openNotification('error', 'Error', `Failed to get site name.`);
    },
  });

  const { data: notAddedData, isFetching: isFetchingNotAdded } = useQuery<ResGetSiteName[]>(
    'get_site_names_not_added',
    getConfigureSitesNamesNotAdded,
    {
      refetchOnWindowFocus: false,
      // refetchOnMount: false,
      // refetchOnMount: true,
      // initialData: [],
      enabled: type === 'add' && notAdded,
      onError: () => {
        queryClient.setQueryData('get_site_names_not_added', []);
        openNotification('error', 'Error', `Failed to get site name.`);
      },
    }
  );
  const selectSite = useSelector(remoteJobSiteSelector);
  const queryClient = useQueryClient();

  const refreshSiteName = useCallback(() => {
    queryClient.fetchQuery('get_site_names');
  }, [queryClient]);

  const refreshSiteNameNotAdded = useCallback(() => {
    queryClient.fetchQuery('get_site_names_not_added');
  }, [queryClient]);

  const disabledSelectSite = useMemo(() => {
    if (isFetching) {
      return true;
    } else {
      if (type === 'edit') {
        return !!selectSite;
      } else {
        return false;
      }
    }
  }, [selectSite, type, isFetching]);

  const disabledSelectSiteNotAdded = useMemo(() => {
    if (isFetching) {
      return true;
    } else {
      if (type === 'edit') {
        return !!selectSite;
      } else {
        return false;
      }
    }
  }, [selectSite, type, isFetchingNotAdded]);

  return {
    disabledSelectSite: notAdded ? disabledSelectSiteNotAdded : disabledSelectSite,
    refreshSiteName: notAdded ? refreshSiteNameNotAdded : refreshSiteName,
    isFetching: notAdded ? isFetchingNotAdded : isFetching,
    data: notAdded ? notAddedData : data,
  };
}
