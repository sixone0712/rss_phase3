import {createAction, handleActions} from 'redux-actions';
import {fromJS, List, Map} from 'immutable';
import moment from "moment";
import * as Define from '../define'
import _ from "lodash";

const SEARCH_SET_INIT_ALL_LIST = 'searchList/SEARCH_SET_INIT_ALL_LIST';
const SEARCH_SET_REQUEST_LIST= 'searchList/SEARCH_SET_REQUEST_LIST';
const SEARCH_SET_REQEUST_START_DATE= 'searchList/SEARCH_SET_REQEUST_START_DATE';
const SEARCH_SET_REQUEST_END_DATE= 'searchList/SEARCH_SET_REQUEST_END_DATE';
const SEARCH_SET_REQUEST_FOLDER= 'searchList/SEARCH_SET_REQUEST_FOLDER';

const SEARCH_INIT_RESPONSE_LIST = 'searchList/SEARCH_INIT_RESPONSE_LIST';
const SEARCH_SAVE_RESPONSE_LIST= 'searchList/SEARCH_SAVE_RESPONSE_LIST';
const SEARCH_SET_SORT_QUERY = 'searchList/SEARCH_SET_SORT_QUERY';
const SEARCH_SET_SORT_KEY_DIRECTION = 'searchList/SEARCH_SET_SORT_KEY_DIRECTION';
const SEARCH_SET_SORT_FOLDER_FILE = 'searchList/SEARCH_SET_SORT_FOLDER_FILE';
const SEARCH_CHECK_RESPONSE_LIST = 'searchList/SEARCH_CHECK_RESPONSE_LIST';
const SEARCH_CHECK_ALL_RESPONSE_LIST = 'searchList/SEARCH_CHECK_ALL_RESPONSE_LIST';

export const searchSetInitAllList = createAction(SEARCH_SET_INIT_ALL_LIST);
export const searchSetRequestList = createAction(SEARCH_SET_REQUEST_LIST);
export const searchSetRequestStartDate = createAction(SEARCH_SET_REQEUST_START_DATE);
export const searchSetRequestEndDate = createAction(SEARCH_SET_REQUEST_END_DATE);
export const searchSetRequestFolder = createAction(SEARCH_SET_REQUEST_FOLDER);

export const searchInitResponseList = createAction(SEARCH_INIT_RESPONSE_LIST);
export const searchSaveResponseList = createAction(SEARCH_SAVE_RESPONSE_LIST);
export const searchSetSortQuery = createAction(SEARCH_SET_SORT_QUERY);
export const searchSetSortKeyDirection = createAction(SEARCH_SET_SORT_KEY_DIRECTION);
export const searchSetSortFolderFile = createAction(SEARCH_SET_SORT_FOLDER_FILE);
export const searchCheckResponseList = createAction(SEARCH_CHECK_RESPONSE_LIST);
export const searchCheckALLResponseList = createAction(SEARCH_CHECK_ALL_RESPONSE_LIST);

export const initialState = Map({
    requestCompleted: false,
    requestCompletedDate: "",
    requestListCnt: 0,
    requestList: Map({
        fabNames: List[{}],
        machineNames: List[{}],
        //categoryTypes: List[{}],     //Not currently in use
        categoryCodes: List[{}],
        categoryNames: List[{}],
        startDate: "",
        endDate: "",
        //keyword: "",      //Not currently in use
        //dir: "",      //Not currently in use
        folder: false,
    }),
    downloadCnt: 0,
    responseIsFolder: false,
    responseListCnt: 0,
    responseList: List([
		Map({
            keyIndex: 0,
            structId: "",
            targetName: "",
            logName: "",
            logId: "",
            logIdName: "",
            //fileId: "",  //Not currently in use
            fileName: "",
            fileSize: "",
            fileDate: "",
            filePath: "",
            //fileStatus: "",  //Not currently in use
            //file: "",  //Not currently in use
            fileType: "F",
            checked: false
		})
    ]),
    sortedList: List([]),
    sortFolderFile: Define.SORT_FOLDER_FILE.ALL,
    query: "",
    sortKey : "",
    sortDirection:  "",
    startDate: moment().startOf('day'),
    endDate : moment().endOf('day'),
    isFolder: false,
    searchDepth: Define.FILE_SEARCH_DEPTH_DEFAULT
});

//2020-08-20 07:25
export default handleActions({
    [SEARCH_SET_INIT_ALL_LIST]: (state, action) => {
        return initialState;
    },

    [SEARCH_SET_REQEUST_START_DATE]: (state, action) => {

        const startTime = action.payload;
        //const moment = require("moment");
        //const convDate = moment(startTime).format("YYMMDDHHMMSS");
        console.log("SEARCH_SET_REQEUST_START_DATE");
        //console.log("action.payload", action.payload);

        return state.set("startDate", startTime);
    },

    [SEARCH_SET_REQUEST_END_DATE]: (state, action) => {

        const endDate = action.payload;
        //const moment = require("moment");
        //const convDate = moment(endDate).format("YYMMDDHHMMSS");

        console.log("SEARCH_SET_REQUEST_END_DATE");
        //console.log("action.payload", action.payload);

        return state.set("endDate", endDate);
    },

    [SEARCH_SET_REQUEST_FOLDER]: (state, action) => {
        return state.set("isFolder", action.payload)
                    .set("searchDepth", action.payload ? 0 : Define.FILE_SEARCH_DEPTH_DEFAULT);
    },

    [SEARCH_SET_REQUEST_LIST]: (state, action) => {
        console.log("SEARCH_SET_REQUEST_LIST");
        const { toolList, logInfoList, startDate, endDate } = action.payload;
        const isFolder = !!state.get("isFolder");

        const newToolList = toolList.filter(list => list.get("checked") === true).toJS();
        const newLogInfoList = logInfoList.filter(list => list.get("checked") === true).toJS();
        const formDate = isFolder ? "" : moment(startDate).format("YYYYMMDDHHmmss");
        const toDate = isFolder ? "" : moment(endDate).format("YYYYMMDDHHmmss");

        const fabNames = newToolList.map(list => list.structId);
        const machineNames = newToolList.map(list => list.targetname);
        const categoryCodes = newLogInfoList.map(list => list.logCode);
        const categoryNames = newLogInfoList.map(list => list.logName);

        return state.setIn(['requestList', 'fabNames'], fromJS(fabNames))
                    .setIn(['requestList', 'machineNames'], fromJS(machineNames))
                    .setIn(['requestList', 'categoryCodes'], fromJS(categoryCodes))
                    .setIn(['requestList', 'categoryNames'], fromJS(categoryNames))
                    .setIn(['requestList', 'startDate'], formDate)
                    .setIn(['requestList', 'endDate'], toDate)
                    .setIn(['requestList', 'folder'], isFolder);
    },

    [SEARCH_INIT_RESPONSE_LIST] : (state, action) => {
        return state.set("responseList", List([]))
                    .set("requestListCnt", 0)
                    .set("responseListCnt", 0)
                    .set('downloadCnt', 0)
                    .set('requestCompleted', false)
                    .set('sortedList', List([]))
                    .set('query', '')
                    .set('sortKey', '')
                    .set('sortDirection', '')
                    .set('sortFolderFile', Define.SORT_FOLDER_FILE.ALL);
    },
    [SEARCH_SAVE_RESPONSE_LIST]: (state, action) => {
        const { data: { lists } } = action.payload;

        if(lists) {
            const newLists = lists.map((list, idx) => {
                return {
                    keyIndex: idx,
                    structId: list.fabName,
                    targetName: list.machineName,
                    logName: list.categoryName,
                    logId: list.categoryCode,
                    logIdName: `${list.categoryCode}_${list.categoryName}`,
                    //fileId: list.fileId,  //Not currently in use
                    fileName: list.fileName,
                    fileSize: list.fileSize,
                    fileDate: list.fileDate,
                    filePath: list.filePath,
                    //fileStatus: list.fileStatus,  //Not currently in use
                    //file: list.file,  //Not currently in use
                    fileType: list.fileType,
                    checked: true
                }
            });
            const newDownloadCnt = newLists.length;
            const newResponseList = fromJS(newLists);
            const newSortedList = getSortedKeyDirection({
                lists: newResponseList,
                sortKey: state.get("sortKey"),
                sortDirection: state.get("sortDirection"),
            })
            return state.set('responseList', newResponseList)
                .set('requestListCnt', newDownloadCnt)
                .set('sortedList', newSortedList)
                .set('responseListCnt', newDownloadCnt)
                .set('downloadCnt', newDownloadCnt)
                .set('requestCompletedDate', new Date())
                .set('requestCompleted', true)
                .set('responseIsFolder', state.get("isFolder"));
        } else {
            return state.set("responseList", List([]))
                .set("requestListCnt", 0)
                .set('sortedList', List([]))
                .set("responseListCnt", 0)
                .set('downloadCnt', 0)
                .set('requestCompletedDate', new Date())
                .set('requestCompleted', true)
                .set('responseIsFolder', state.get("isFolder"));
        }
    },

    [SEARCH_SET_SORT_QUERY]: (state, action) => {
        const responseList = state.get("responseList");
        const query = action.payload;
        const sortKey = state.get("sortKey");
        const sortDirection = state.get("sortDirection");
        const sortFolderFile = state.get("sortFolderFile");

        let newSortedList;
        if(query === "") {
            const allCheckList = responseList.map(item => item.set("checked", true));
            const folderFileList = getSortedFolderFile({
                lists: allCheckList,
                sortFolderFile
            })
            newSortedList = getSortedKeyDirection({
                lists: folderFileList,
                sortKey: "",
                sortDirection: ""
            })
        } else {
            const queryList = getSortedAllList({
                lists: responseList,
                query,
                sortKey,
                sortDirection,
                sortFolderFile
            })
            newSortedList = queryList.map(item => item.set("checked", true));
        }

        return state.set("query", query)
                    .set("sortedList", newSortedList)
                    .set("downloadCnt", newSortedList.size)
                    .set("sortKey", query ? sortKey : "")
                    .set("sortDirection", query ? sortDirection : "");
    },
    [SEARCH_SET_SORT_KEY_DIRECTION]: (state, action) => {
        const sortedList = state.get("sortedList");
        const { sortKey, sortDirection } = action.payload;
        console.log("chpark_sortKey", sortKey);
        console.log("chpark_sortDirection", sortDirection);
        const newSortedList = getSortedKeyDirection({
            lists: sortedList,
            sortKey,
            sortDirection,
        })

        console.log("chpark_newSortedList", newSortedList.toJS());

        return state.set("sortKey", sortKey)
                    .set("sortDirection", sortDirection)
                    .set("sortedList", newSortedList);
    },
    [SEARCH_SET_SORT_FOLDER_FILE]: (state, action) => {
        const sortFolderFile = action.payload;
        const responseList = state.get("responseList");
        const query = state.get("query");
        const sortKey = state.get("sortKey");
        const sortDirection=  state.get("sortDirection");

        let newSortedList;
        if(query === "") {
            const allCheckList = responseList.map(item => item.set("checked", true));
            const folderFileList = getSortedFolderFile({
                lists: allCheckList,
                sortFolderFile
            })
            newSortedList = getSortedKeyDirection({
                lists: folderFileList,
                sortKey: "",
                sortDirection: ""
            })
        } else {
            const queryList = getSortedAllList({
                lists: responseList,
                query,
                sortKey,
                sortDirection,
                sortFolderFile
            })
            newSortedList = queryList.map(item => item.set("checked", true));
        }

        return state.set("sortFolderFile", sortFolderFile)
                    .set("sortedList", newSortedList)
                    .set("downloadCnt", newSortedList.size)
                    .set("sortKey", query ? sortKey : "")
                    .set("sortDirection", query ? sortDirection : "");
    },
    [SEARCH_CHECK_RESPONSE_LIST]: (state, action) => {
        const sortedList = state.get("sortedList");
        let downloadCnt = state.get("downloadCnt");
        const keyIndex = +action.payload;
        const findArrIndex = sortedList.findKey(item => {
            return item.get("keyIndex") === keyIndex;
        });
        const check = sortedList.getIn([findArrIndex, 'checked']);

        if(check){
            downloadCnt--;
        } else {
            downloadCnt++;
        }

        return state.set('downloadCnt', downloadCnt)
            .set("sortedList", sortedList.update(findArrIndex, list => list.set("checked", !list.get("checked"))))
    },

    [SEARCH_CHECK_ALL_RESPONSE_LIST] : (state, action) => {
        const check = action.payload;
        const sortedList = state.get("sortedList");
        const newSortedList = sortedList.map(item => item.set("checked", check));
        return state.set("sortedList", newSortedList)
                    .set("downloadCnt", check ? newSortedList.size : 0);
    },

}, initialState)


function getSortedAllList({ lists, query, sortKey, sortDirection, sortFolderFile}) {
    const listsToJs = lists.toJS();
    const queryList = query
        ? listsToJs.filter((list) => {
            if(sortFolderFile === Define.SORT_FOLDER_FILE.FILE)
                return list.fileName.toLowerCase().includes(query.toLowerCase()) && list.fileType === "F"
            else if(sortFolderFile === Define.SORT_FOLDER_FILE.FOLDER)
                return list.fileName.toLowerCase().includes(query.toLowerCase()) && list.fileType !== "F"
            else
                return list.fileName.toLowerCase().includes(query.toLowerCase())
        })
        : listsToJs;
    const tempKey = sortKey === "" ? "targetName" : sortKey;
    const tempSortDirection = sortDirection === "" ? "asc" : sortDirection;
    const sortedList = _.orderBy(queryList, tempKey, tempSortDirection);
    return fromJS(sortedList);
}

function getSortedQueryList({ lists, query }) {
    const listsToJs = lists.toJS();
    const sortedList = query
        ? listsToJs.filter((list) => list.fileName.toLowerCase().includes(query.toLowerCase()))
        : listsToJs;
    return fromJS(sortedList);
}

function getSortedKeyDirection({ lists, sortKey, sortDirection}) {
    const listsToJs = lists.toJS();
    const tempKey = sortKey === "" ? "targetName" : sortKey;
    const tempSortDirection = sortDirection === "" ? "asc" : sortDirection;
    const sortedList =  _.orderBy(listsToJs, tempKey, tempSortDirection);
    return fromJS(sortedList);
}

function getSortedFolderFile({ lists, sortFolderFile}) {
    const listsToJs = lists.toJS();
    const sortedList = sortFolderFile === Define.SORT_FOLDER_FILE.ALL
        ? listsToJs
        : listsToJs.filter((list) => {
            if(sortFolderFile === Define.SORT_FOLDER_FILE.FOLDER)
                return list.fileType !== 'F';
            else if(sortFolderFile === Define.SORT_FOLDER_FILE.FILE)
                return list.fileType === 'F';
            return true;
        });
    return fromJS(sortedList);
}