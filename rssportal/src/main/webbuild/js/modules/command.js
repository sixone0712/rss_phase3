import {createAction, handleActions} from 'redux-actions';
import {fromJS, List, Map} from 'immutable';
import services from '../services';
import {applyPenders} from 'redux-pender';
import * as Define from "../define";

const COMMAND_INIT = "cmd/COMMAND_INIT";
const COMMAND_LOAD_LIST = "cmd/COMMAND_GET_LIST";
const COMMAND_CHECK_ONLY_ONE_LIST = "cmd/COMMAND_CHECK_ONLY_ONE_LIST";
const COMMAND_CHECK_LIST = "cmd/COMMAND_CHECK_LIST";
const COMMAND_CHECK_ALL_LIST = "cmd/COMMAND_CHECK_ALL_LIST";
const COMMAND_CHECK_INIT ="cmd/COMMAND_CHECK_INIT";
const COMMAND_DELETE_ITEM = "cmd/COMMAND_DELETE_ITEM";
const COMMAND_ADD_NOT_USE = "cmd/COMMAND_ADD_NOT_USE";

export const commandInit = createAction(COMMAND_INIT);
export const commandLoadList = createAction(COMMAND_LOAD_LIST, services.axiosAPI.requestGet);
export const commandCheckOnlyOneList = createAction(COMMAND_CHECK_ONLY_ONE_LIST);
export const commandCheckList = createAction(COMMAND_CHECK_LIST);
export const commandCheckAllList = createAction(COMMAND_CHECK_ALL_LIST);
export const commandCheckInit = createAction(COMMAND_CHECK_INIT);
export const commandAddNotUse = createAction(COMMAND_ADD_NOT_USE);
export const commandDeleteItem = createAction(COMMAND_DELETE_ITEM);

export const initialState = Map({
    command: Map({
        error: "",
        checkedCnt: 0,
        totalCnt: 0,
        checkedLists: List([]),
        lists: List([])
        /*
        lists: [
            {
                index: 0
                id: 1
                cmd_name: "TEST_1"
                cmd_type: "vftp_sss"
                checked: false
            }
        ]
        */
    }),
});

const reducer = handleActions({
    [COMMAND_INIT]: () => {
        console.log("COMMAND_INIT");
        return initialState;
    },
    [COMMAND_CHECK_ONLY_ONE_LIST]: (state, action) => {
        const id = +action.payload;      // to number
        const commandList = state.getIn(["command", "lists"]);
        const allFalseList = commandList.map(list => list.set("checked", false));
        const newCheckedList = [];
        let newCommandList;
        let newCheckedCnt = 0;

        if (id !== -1) {
            const findIndex = allFalseList.findIndex(item => item.get("id") === id);
            newCommandList = allFalseList.update(findIndex, list => list.set("checked", true));
            newCheckedList.push(id);
            newCheckedCnt = 1;
        }

        return state.setIn(["command", "lists"], id !== -1 ? newCommandList : allFalseList)
                    .setIn(["command", "checkedLists"], newCheckedList)
                    .setIn(["command", "checkedCnt"], newCheckedCnt);
    },
    [COMMAND_CHECK_LIST]: (state, action) => {
        const id = +action.payload;
        const command = state.get("command");
        const lists = command.get("lists");
        let checkedCnt = command.get("checkedCnt");
        const findIndex = lists.findIndex(item => item.get("id") === id);
        const checked = lists.getIn([findIndex, "checked"]);
        const newLists = lists.update(findIndex, list => list.set("checked", !checked));
        const newCheckedList = [];
        newLists.map(list => {
            if (list.get("checked") === true) {
                newCheckedList.push(list.get("id"))
            }
        });

        return state.setIn(["command", "lists"], newLists)
                    .setIn(["command", "checkedCnt"], checked ? checkedCnt - 1 : checkedCnt + 1)
                    .setIn(["command", "checkedLists"], newCheckedList);
    },
    [COMMAND_CHECK_ALL_LIST]: (state, action) => {
        const regex = /(-)|(%s)/g;
        const { check, query } = action.payload;
        const lists = state.getIn(["command", "lists"]);
        const newLists = lists.map(list => list.set("checked",
            list.get("cmd_name").toLowerCase().replace(regex, "").includes(query.toLowerCase()) ? check : list.get("checked")));
        const newCheckedList = [];
        let checkedCnt = 0;

        newLists.map(list => {
           if (list.get("checked")) {
               newCheckedList.push(list.get("id"));
               checkedCnt++;
           }
        });

        return state.setIn(["command", "lists"], newLists)
                    .setIn(["command", "checkedLists"], newCheckedList)
                    .setIn(["command", "checkedCnt"], checkedCnt);
    },
    [COMMAND_CHECK_INIT]: (state, action) => {
        const savedCommands = action.payload;
        const lists = state.getIn(["command", "lists"]);

        if (savedCommands.length === 0) { return state.setIn(["command", "lists"], lists); }

        const newList = lists.map(list => list.set("checked", savedCommands.includes(list.get("cmd_name"))));
        const newCheckedList = [];
        let realCheckCnt = 0;

        lists.map(list => {
           if (savedCommands.includes(list.get("cmd_name"))) {
               newCheckedList.push(list.get("id"));
               realCheckCnt++;
           }
        });

        return state.setIn(["command", "lists"], newList)
            .setIn(["command", "checkedLists"], newCheckedList)
            .setIn(["command", "checkedCnt"], realCheckCnt);
    },
    [COMMAND_ADD_NOT_USE]: (state) => {
        const lists = state.getIn(["command", "lists"]);
        const checkedList = state.getIn(["command", "checkedLists"]);
        const newLists = [
            {
                index: -1,
                id: -1,
                cmd_name: "none",
                cmd_type: "vftp_compat",
                checked: checkedList.includes(-1)
            }
        ];

        lists.map(list => newLists.push(list));

        return state.setIn(["command", "lists"], fromJS(newLists))
                    .setIn(["command", "totalCnt"], newLists.length);
    },
    //BTS 10861
    [COMMAND_DELETE_ITEM]: (state, action) => {
        const { id, planType } = action.payload;
        const checkedCnt = state.getIn(["command", "checkedCnt"]);
        const totalCnt = state.getIn(["command", "totalCnt"]);
        const lists = state.getIn(["command", "lists"]);
        const findIndex = 
            planType === Define.PLAN_TYPE_VFTP_COMPAT ?
                lists.findIndex(item => item.get("id") === id) - 1
                : lists.findIndex(item => item.get("id") === id);
        const checked = lists.getIn([findIndex, "checked"]);
        const newCheckedList = [];
        lists.map(list => {
            if (list.get("index") !== findIndex && list.get("checked") === true) {
                newCheckedList.push(list.get("id"));
            }
        });

        return state.setIn(["command", "checkedLists"], newCheckedList)
                    .setIn(["command", "checkedCnt"], checked ? checkedCnt - 1 : checkedCnt)
                    .setIn(["command", "totalCnt"], totalCnt - 1);
    }
}, initialState);

export default applyPenders(reducer, [
    {
        type: COMMAND_LOAD_LIST,
        onSuccess: (state, action) => {
            const { lists } = action.payload.data;
            const checkedList = state.getIn(["command", "checkedLists"]);
            const commandList = lists.map((item, idx) => ({
                index: idx,
                id: item.id,
                cmd_name: item.cmd_name,
                cmd_type: item.cmd_type,
                checked: checkedList.includes(item.id)
            }));

            const totalCnt = commandList.length;

            return state.setIn(["command", "lists"], fromJS(commandList))
              .setIn(["command", "totalCnt"], totalCnt)
              .setIn(["command", "error"], "");
        },
        onFailure: (state, action) => {
            const { error: { reason } } = action.payload.response.data;
            return state.setIn(["command", "error"], reason);
        }
    },
])

