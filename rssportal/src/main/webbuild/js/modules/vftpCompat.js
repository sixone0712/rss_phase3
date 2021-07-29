import {createAction, handleActions} from 'redux-actions';
import {List, Map} from 'immutable';
import moment from "moment";

const VFTP_COMPAT_INIT_ALL = 'vftpCompat/VFTP_COMPAT_INIT_ALL';
const VFTP_COMPAT_SET_REQUEST_MACHINE= 'vftpCompat/VFTP_COMPAT_SET_REQUEST_MACHINE';
const VFTP_COMPAT_SET_REQUEST_COMMAND= 'vftpCompat/VFTP_COMPAT_SET_REQUEST_COMMAND';
const VFTP_COMPAT_SET_REQEUST_START_DATE= 'vftpCompat/VFTP_COMPAT_SET_REQEUST_START_DATE';
const VFTP_COMPAT_SET_REQUEST_END_DATE= 'vftpCompat/VFTP_COMPAT_SET_REQUEST_END_DATE';

export const vftpCompatInitAll = createAction(VFTP_COMPAT_INIT_ALL); //initialize....
export const vftpCompatSetRequestMachine = createAction(VFTP_COMPAT_SET_REQUEST_MACHINE); 	// machine
export const vftpCompatSetRequestCommand = createAction(VFTP_COMPAT_SET_REQUEST_COMMAND); 	// command
export const vftpCompatSetRequestStartDate = createAction(VFTP_COMPAT_SET_REQEUST_START_DATE); 	// startDate
export const vftpCompatSetRequestEndDate = createAction(VFTP_COMPAT_SET_REQUEST_END_DATE); 	// endDate

export const initialState = Map({
    requestCompletedDate: "",
    requestListCnt: 0,
    requestList: Map({
        fabNames: List[{}],
        machineNames: List[{}],
        command: "",
    }),
    startDate: moment().startOf('day'),
    endDate: moment().endOf('day')
});


export default handleActions({
    [VFTP_COMPAT_INIT_ALL]: (state, action) => {
        return initialState;
    },

    [VFTP_COMPAT_SET_REQUEST_MACHINE]: (state, action) => {
        const { fabNames, machineNames } = action.payload;
        return state.setIn(["requestList","fabNames"], fabNames)
                    .setIn(["requestList", "machineNames"], machineNames);
    },

    [VFTP_COMPAT_SET_REQUEST_COMMAND]: (state, action) => {
        const command = action.payload;
        return state.setIn(["requestList","command"], command);
    },

    [VFTP_COMPAT_SET_REQEUST_START_DATE]: (state, action) => {
        const startDate = action.payload;
        return state.set("startDate", startDate);
    },

    [VFTP_COMPAT_SET_REQUEST_END_DATE]: (state, action) => {
        const endDate = action.payload;
        return state.set("endDate", endDate);
    },
}, initialState)
