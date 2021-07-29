export const getToolInfoList = (props) => {
    const { toolInfoList } = props;
    return toolInfoList.toJS();
};

export const checkToolInfoList = (props, idx) => {
    const { viewListActions } = props;

    console.log("checkToolInfoList");
    console.log("props", props);
    console.log("viewListActions", viewListActions);
    console.log("idx", idx);
    viewListActions.viewCheckToolList(idx);
};

export const checkAllToolInfoList  = (props, isAllCheck, isManual) => {
    const { viewListActions } = props;

    if(isAllCheck === true) {
        viewListActions.viewCheckAllToolList({
            isAllCheck: true,
            isManual,
        });
    } else {
        viewListActions.viewCheckAllToolList({
            isAllCheck: false,
            isManual,
        });
    }
};
