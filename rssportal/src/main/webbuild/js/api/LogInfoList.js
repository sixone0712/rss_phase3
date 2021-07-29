export const getLogInfoList = (props) => {
    const { logInfoList } = props;
    return logInfoList.toJS();
};

export const checkLogInfoList = (props, idx) => {
    const { viewListActions } = props;
    viewListActions.viewCheckLogTypeList(idx);
};

export const checkAllLogInfoList = (props, isAllChecked, actionList) => {
    const { viewListActions } = props;
    viewListActions.viewCheckAllLogTypeList({ check: isAllChecked, actionList: actionList });
};