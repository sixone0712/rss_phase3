import { useCallback, useEffect, useState } from 'react';
import { useQuery, useQueryClient } from 'react-query';
import { useSelector } from 'react-redux';
import { openNotification } from '../lib/util/notification';
import { getRemotePlans } from '../lib/api/axios/requests';
import { remoteJobSiteSelector } from '../reducers/slices/remoteJob';
import { RemotePlan } from '../types/status';

export interface ResAutoPlanType {
  planId: number;
  planType: string;
  ownerId: number;
  planName: string;
  fabNames: string[];
  machineNames: string[];
  categoryNames: string[];
  categoryCodes: string[];
  commands: string[];
  type: string;
  interval: number;
  description: string;
  start: string;
  from: string;
  to: string;
  lastCollection: string;
  status: string;
  detailedStatus: string;
}

export interface AutoPlanType {
  key: number;
  planId: number;
  planName: string;
  planType: string;
  description: string;
  status: string;
  detailedStatus: string;
  machines: number;
  machineNames: string[];
  targets: number;
  targetNames: string[];
}

export default function usePlansSetting() {
  const selectSite = useSelector(remoteJobSiteSelector);
  const queryClient = useQueryClient();
  const { data: plans, isFetching, isError } = useQuery(
    ['get_remote_plans', selectSite?.value],
    () => getRemotePlans(selectSite?.value as string),
    {
      refetchOnWindowFocus: false,
      // refetchOnMount: false,
      enabled: !!selectSite?.value,
      initialData: [] as RemotePlan[],
      onError: () => {
        queryClient.setQueryData(['get_remote_plans', selectSite?.value], []);
        openNotification('error', 'Error', `Failed to get auto plan list of "${selectSite?.label}".`);
      },
    }
  );

  const refreshPlans = useCallback(() => {
    if (selectSite?.value !== undefined) queryClient.fetchQuery(['get_remote_plans', selectSite?.value]);
  }, [queryClient, selectSite]);

  return {
    plans,
    isFetching,
    refreshPlans,
    usePlansSetting,
  };
}
