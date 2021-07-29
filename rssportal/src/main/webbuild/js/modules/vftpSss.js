import {createAction, handleActions} from 'redux-actions';
import {fromJS, List, Map} from 'immutable';
import moment from "moment";

const VFTP_SSS_INIT_ALL = 'vftpSss/VFTP_SSS_INIT_ALL';
const VFTP_SSS_SET_REQUEST_MACHINE= 'vftpSss/VFTP_SSS_SET_REQUEST_MACHINE';
const VFTP_SSS_SET_REQUEST_COMMAND= 'vftpSss/VFTP_SSS_SET_REQUEST_COMMAND';
const VFTP_SSS_SET_REQEUST_START_DATE= 'vftpSss/VFTP_SSS_SET_REQEUST_START_DATE';
const VFTP_SSS_SET_REQUEST_END_DATE= 'vftpSss/VFTP_SSS_SET_REQUEST_END_DATE';
const VFTP_SSS_SET_RESPONSE_LIST= 'vftpSss/VFTP_SSS_SET_RESPONSE_LIST';
const VFTP_SSS_INIT_RESPONSE_LIST = 'vftpSss/VFTP_SSS_INIT_RESPONSE_LIST';
const VFTP_SSS_SET_IS_NEW_RESPONSE_LIST = 'vftpSss/VFTP_SSS_SET_IS_NEW_RESPONSE_LIST';

export const vftpSssInitAll = createAction(VFTP_SSS_INIT_ALL); //initialize....
export const vftpSssSetRequestMachine = createAction(VFTP_SSS_SET_REQUEST_MACHINE); 	// machine
export const vftpSssSetRequestCommand = createAction(VFTP_SSS_SET_REQUEST_COMMAND); 	// command
export const vftpSssSetRequestStartDate = createAction(VFTP_SSS_SET_REQEUST_START_DATE); 	// startDate
export const vftpSssSetRequestEndDate = createAction(VFTP_SSS_SET_REQUEST_END_DATE); 	// endDate
export const vftpSssSetResponseList = createAction(VFTP_SSS_SET_RESPONSE_LIST);
export const vftpSssInitResponseList = createAction(VFTP_SSS_INIT_RESPONSE_LIST);
export const vftpSssSetIsNewResponseList = createAction(VFTP_SSS_SET_IS_NEW_RESPONSE_LIST);

export const initialState = Map({
    requestCompletedDate: "",
    requestListCnt: 0,
    requestList: Map({
        fabNames: List[{}],
        machineNames: List[{}],
        command: "",
    }),
    isNewResponseList: false,
    responseList: List[{}],
    responseListCnt: 0,
    downloadStatus: Map({
        func: null,
        dlId: "",
        status: "init",
        totalFiles: 0,
        downloadedFiles: 0,
        totalSize: 0,
        downloadSize: 0,
        downloadUrl: ""
    }),
    startDate: moment().startOf('day'),
    endDate: moment().endOf('day')
});


export default handleActions({
    [VFTP_SSS_INIT_ALL]: (state, action) => {
        return initialState;
    },

    [VFTP_SSS_SET_REQUEST_MACHINE]: (state, action) => {
        const { fabNames, machineNames } = action.payload;
        console.log("VFTP_SSS_SET_REQUEST_MACHINE");

        return state
            .setIn(["requestList","fabNames"], fabNames)
            .setIn(["requestList", "machineNames"], machineNames);
    },

    [VFTP_SSS_SET_REQUEST_COMMAND]: (state, action) => {
        const command = action.payload;
        console.log("VFTP_SSS_SET_REQUEST_COMMAND");
        return state.setIn(["requestList","command"], command);
    },

    [VFTP_SSS_SET_REQEUST_START_DATE]: (state, action) => {
        const startDate = action.payload;
        console.log("VFTP_SSS_SET_REQEUST_START_DATE");
        return state.set("startDate", startDate);
    },

    [VFTP_SSS_SET_REQUEST_END_DATE]: (state, action) => {
        const endDate = action.payload;
        console.log("SEARCH_SET_REQUEST_END_DATE");
        return state.set("endDate", endDate);
    },
    [VFTP_SSS_SET_RESPONSE_LIST]: (state, action) => {
        const lists = action.payload.data.lists;
        const newList = lists.map((item, idx) => {
            item.index = idx;
            item.checked = true;
            return item;
        })

        return state.set("responseList", fromJS(newList))
                    .set('responseListCnt', newList.length)
                    .set('isNewResponseList', true)
                    .set('requestCompletedDate', new Date());
    },
    [VFTP_SSS_INIT_RESPONSE_LIST] : (state, action) => {
        return state.set("responseList", List([]))
                    .set("requestListCnt", 0)
                    .set("responseListCnt", 0)
                    .set('isNewResponseList', false);
    },

    [VFTP_SSS_SET_IS_NEW_RESPONSE_LIST] : (state, action) => {
        const check = action.payload;
        return state.set("isNewResponseList", check);
    },
}, initialState)
