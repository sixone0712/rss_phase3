import { LabeledValue } from 'antd/lib/select';
import { useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { initLocalJobAction, localJobSiteSelector, selectSiteAction } from '../reducers/slices/localJob';

export default function useLocalJobSlices() {
  const dispatch = useDispatch();
  const selectSite = useSelector(localJobSiteSelector);
  const setSelectSite = useCallback(
    ({ value, label }: LabeledValue) => {
      dispatch(selectSiteAction({ value, label }));
    },
    [dispatch]
  );

  const initLocalJobState = useCallback(() => {
    dispatch(initLocalJobAction());
  }, [dispatch]);

  return {
    selectSite,
    initLocalJobState,
    setSelectSite,
  };
}
