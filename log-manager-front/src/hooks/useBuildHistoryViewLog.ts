import { useEffect, useMemo, useState } from 'react';
import { useSelector } from 'react-redux';
import { API_URL } from '../lib/constants';
import { buildHistorySelectedJob, buildHistorySelectedLog } from '../reducers/slices/buildHistory';

export default function useBuildHistoryViewLog() {
  const selectedJob = useSelector(buildHistorySelectedJob);
  const selectedLog = useSelector(buildHistorySelectedLog);
  const [requestUrl, setRequestUrl] = useState<string | undefined>(undefined);
  const status = useMemo(() => selectedLog?.status, [selectedLog?.status]);
  const name = useMemo(() => selectedLog?.name, [selectedLog?.name]);
  useEffect(() => {
    if (selectedJob && selectedLog) {
      const { type, jobId, stepType } = selectedJob;
      const { id } = selectedLog;
      if (type && jobId && stepType && id)
        setRequestUrl(API_URL.GET_STATUS_BUILD_HISTORY_LOG({ jobId, type, stepType, id }));
      else console.error(jobId, type, stepType, id);
    } else {
      setRequestUrl(undefined);
    }
  }, [selectedJob, selectedLog]);

  return {
    requestUrl,
    status,
    name,
  };
}
