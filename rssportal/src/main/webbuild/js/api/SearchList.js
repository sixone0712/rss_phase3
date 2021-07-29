import * as Define from "../define";

export const getRequestList = (props) => {
    const { requestList } = props;
    return requestList.toJS();
};

export const setStartDate = (props, date) => {
    const { searchListActions } = props;
    searchListActions.searchSetRequestStartDate(date);
};

export const setEndDate = (props, date) => {
    const { searchListActions } = props;
    searchListActions.searchSetRequestEndDate(date);
};

export const setSearchList = async (props) => {
    const {
        searchListActions,
        toolInfoList: toolList,
        logInfoList,
        startDate,
        endDate,
        isFolder,
        toolInfoListCheckCnt,
        logInfoListCheckCnt } = props;

    if(toolInfoListCheckCnt <= 0 || logInfoListCheckCnt <= 0) {
        return Define.SEARCH_FAIL_NO_MACHINE_AND_CATEGORY
    }

    if(!isFolder) {
        if(startDate.isAfter(endDate)) return Define.SEARCH_FAIL_DATE;
    }

    await searchListActions.searchSetRequestList({toolList, logInfoList, startDate, endDate});
    return Define.RSS_SUCCESS;
};

export const getResponseList = (props) => {
    const { responseList } = props;
    return responseList.toJS();
};

export const getResponseListCnt = (props) => {
    const { responseListCnt } = props;
    return responseListCnt;
};

export const convertDateFormat = (date) => {
    if(date == "" || date == null) return "0000/00/00 00:00:00";

    const year = date.substr(0,4);
    const month = date.substr(4,2);
    const day = date.substr(6,2);
    const hour = date.substr(8,2);
    const min = date.substr(10,2);
    const sec = date.substr(12,2);

    return year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
};

export const bytesToSize = (bytes) => {
    var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    if (bytes == 0) return 'n/a';
    var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
    if (i == 0) return bytes + ' ' + sizes[i];
    return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + sizes[i];
};