import { useCallback, useEffect, useMemo, useState } from 'react';
import { useQuery } from 'react-query';
import { useDispatch, useSelector } from 'react-redux';
import { useLocation, useParams } from 'react-router-dom';
import { getHistoryBuildList } from '../lib/api/axios/requests';
import { ResGetBuildHistoryList } from '../lib/api/axios/types';
import { LOG_HISTORY_MAX_LIST_COUNT, PAGE_URL } from '../lib/constants';
import { openNotification } from '../lib/util/notification';
import {
  buildHistorySelectedJob,
  buildHistorySelectedLog,
  BuildHistorySelectedLogState,
  setBuildHistorySelectedJob,
  setBuildHistorySelectedLog,
} from '../reducers/slices/buildHistory';
import { StatusStepType, StatusType } from '../types/status';

interface BuildHistoryParams {
  id: string;
}
export default function useBuildHistoryMenu() {
  const { pathname } = useLocation();
  const { id: jobId } = useParams<BuildHistoryParams>();
  const { type, stepType } = useMemo(() => getHistoryType(pathname), [pathname]);
  const selectedJob = useSelector(buildHistorySelectedJob);
  const selectedLog = useSelector(buildHistorySelectedLog);
  const dispatch = useDispatch();
  const { data, isFetching } = useQuery<ResGetBuildHistoryList[]>(
    ['get_build_history_list', selectedJob],
    () =>
      getHistoryBuildList({
        jobId: selectedJob?.jobId as string,
        type: selectedJob?.type as StatusType,
        stepType: selectedJob?.stepType as StatusStepType,
      }),
    {
      // retryOnMount: false,
      // refetchOnMount: false,
      refetchOnWindowFocus: false,
      initialData: undefined,
      enabled: !!selectedJob,
      onError: () => {
        openNotification('error', 'Error', `Failed to get build history list`);
      },
    }
  );

  const [currentPage, setCurrentPage] = useState(1);
  const onChangeCurrentPage = useCallback((page: number, pageSize?: number) => {
    setCurrentPage(page);
  }, []);
  const [historyList, setHistoryList] = useState<ResGetBuildHistoryList[]>([]);
  const totalHistoryListLen = useMemo(() => data?.length ?? 0, [data]);

  useEffect(() => {
    if (data?.length) {
      const startIdx = currentPage === 1 ? 0 : (currentPage - 1) * LOG_HISTORY_MAX_LIST_COUNT;
      const newLocal = startIdx + LOG_HISTORY_MAX_LIST_COUNT - 1;
      const endIdx = newLocal;
      setHistoryList(data.slice(startIdx, endIdx));
    } else {
      setHistoryList([]);
    }
  }, [currentPage, data]);

  const setSelectedLog = useCallback(
    (value: BuildHistorySelectedLogState | undefined) => {
      dispatch(setBuildHistorySelectedLog(value));
    },
    [dispatch]
  );

  useEffect(() => {
    dispatch(setBuildHistorySelectedLog(undefined));
  }, []);

  useEffect(() => {
    dispatch(setBuildHistorySelectedJob({ jobId, type, stepType }));
  }, [jobId, type, stepType]);

  return {
    selectedLog,
    setSelectedLog,
    isFetching,
    currentPage,
    totalHistoryListLen,
    historyList,
    onChangeCurrentPage,
  };
}

interface ReturnHistoryType {
  type: StatusType | undefined;
  stepType: StatusStepType | undefined;
}
function getHistoryType(pathname: string): ReturnHistoryType {
  if (pathname.startsWith(PAGE_URL.STATUS_REMOTE_BUILD_HISTORY_CONVERT)) return { type: 'remote', stepType: 'convert' };
  else if (pathname.startsWith(PAGE_URL.STATUS_REMOTE_BUILD_HISTORY_ERROR))
    return { type: 'remote', stepType: 'error' };
  else if (pathname.startsWith(PAGE_URL.STATUS_REMOTE_BUILD_HISTORY_CRAS)) return { type: 'remote', stepType: 'cras' };
  else if (pathname.startsWith(PAGE_URL.STATUS_REMOTE_BUILD_HISTORY_VERSION))
    return { type: 'remote', stepType: 'version' };
  else if (pathname.startsWith(PAGE_URL.STATUS_LOCAL_BUILD_HISTORY_CONVERT))
    return { type: 'local', stepType: 'convert' };
  return { type: undefined, stepType: undefined };
}
