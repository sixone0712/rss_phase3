import {createAction, handleActions} from 'redux-actions';
import {fromJS, List, Map} from 'immutable';
import {pender} from 'redux-pender';
import services from '../services';

const VIEW_INIT_ALL_LIST= 'viewList/VIEW_INIT_ALL_LIST';
const VIEW_SET_DISPLAY = 'viewList/VIEW_SET_DISPLAY';
const VIEW_UPDATE_MANUAL_TOOLINFO_LIST= 'viewList/VIEW_UPDATE_MANUAL_TOOLINFO_LIST';
const VIEW_UPDATE_AUTO_TOOLINFO_LIST= 'viewList/VIEW_UPDATE_AUTO_TOOLINFO_LIST';
const VIEW_LOAD_TOOLINFO_LIST= 'viewList/VIEW_LOAD_TOOLINFO_LIST';
const VIEW_LOAD_LOGTYPE_LIST= 'viewList/VIEW_LOAD_LOGTYPE_LIST';
const VIEW_CHECK_TOOL_LIST = 'viewList/VIEW_CHECK_TOOL_LIST';
const VIEW_CHECK_ALL_TOOL_LIST = 'viewList/VIEW_CHECK_ALL_TOOL_LIST';
const VIEW_CHECK_LOGTYPE_LIST= 'viewList/VIEW_CHECK_LOGTYPE_LIST';
const VIEW_CHECK_ALL_LOGTYPE_LIST= 'viewList/VIEW_CHECK_ALL_LOGTYPE_LIST';
const VIEW_APPLY_GENRE_LIST= 'viewList/VIEW_APPLY_GENRE_LIST';
const VIEW_SET_EDIT_PLAN_LIST= 'viewList/VIEW_SET_EDIT_PLAN_LIST';

export const viewInitAllList = createAction(VIEW_INIT_ALL_LIST);
export const viewSetDisplay = createAction(VIEW_SET_DISPLAY);
export const viewUpdateManualToolInfoList = createAction(VIEW_UPDATE_MANUAL_TOOLINFO_LIST, services.axiosAPI.requestGet);	// getURL
export const viewUpdateAutoToolInfoList = createAction(VIEW_UPDATE_AUTO_TOOLINFO_LIST, services.axiosAPI.requestGet);	// getURL
export const viewLoadToolInfoList = createAction(VIEW_LOAD_TOOLINFO_LIST, services.axiosAPI.requestGet);	// getURL
export const viewLoadLogTypeList = createAction(VIEW_LOAD_LOGTYPE_LIST, services.axiosAPI.requestGet);		// getURL
export const viewCheckToolList = createAction(VIEW_CHECK_TOOL_LIST); 	// index
export const viewCheckAllToolList = createAction(VIEW_CHECK_ALL_TOOL_LIST);		// check
export const viewCheckLogTypeList = createAction(VIEW_CHECK_LOGTYPE_LIST); 	// index
export const viewCheckAllLogTypeList = createAction(VIEW_CHECK_ALL_LOGTYPE_LIST);		// check
export const viewApplyGenreList = createAction(VIEW_APPLY_GENRE_LIST); 	// { index, genrelist }
export const viewSetEditPlanList = createAction(VIEW_SET_EDIT_PLAN_LIST);

export const initialState = Map({
	gotReady: false,	// flag for loaded data
	equipmentList: List([
		Map({
			keyIndex: 0,
			fabName: ""
		})
	]),

	toolInfoDisplay: 'ftp',
	toolInfoListCheckCnt: 0,
	toolInfoVer: '-',
	toolInfoList: List([
		Map({
			keyIndex: 0,
			structId: "",
			targetname: "",
			host: "",
			ots: "",
			line: "",
			serialNumber: "",
			port: "",
			ftpUser: "",
			ftpPassword: "",
			vftpUser: "",
			vftpPassword: "",
			toolType: "",
			ftpConnected : false,
			vftpConnected: false,
			checked: false
		})
	]),

	logInfoListCheckCnt: 0,
	logInfoVer: '-',
	logInfoList: List([
		Map({
			keyIndex: 0,
			logCode: "",
			logName: "",
			description: "",
			dest:"",
			filePath: "",
			fileName: "",
			auto: "",
			display:"",
			checked: false
		})
	]),

	editPlanList: Map({
		id: "",
		planId: "",
		planDescription: "",
		planTarget: "",
		planPeriod: "",
		planStatus: "",
		planLastRun: "",
		planDetail: "",
		tool: List([]),
		logType: List([]),
		separatedZip: false
	})

});

export default handleActions({
	...pender(
		{
			type: VIEW_LOAD_TOOLINFO_LIST, // If type is given, create an object containing action handlers suffixed to this type.

			// In case of request / failure, if there is additional work to be done, add onPending and onFailure like this.
			// onPending: (state, action) => state,
			// onFailure: (state, action) => state
			onSuccess: (state, action) => { // If there is nothing else to do when successful, this function can also be omitted.
				console.log("handleActions[VIEW_LOAD_TOOLINFO_LIST]");
				const { lists, version } = action.payload.data;
				const equipArray = Array.from(new Set(lists.map(item => item.line).filter(v => v !== null)));
				console.log("handleActions[VIEW_LOAD_TOOLINFO_LIST]equipArray", equipArray);
				const  equipList = equipArray.map((item, idx) => ({ keyIndex : idx, fabName: item}));
				console.log("handleActions[VIEW_LOAD_TOOLINFO_LIST]equipList", equipList);
				const toolInfoList = lists.map((list, idx) => {
					return {
						keyIndex: idx,
						structId: list.line,
						//collectServerId: list.collectServerId,    //Not currently in use
						//collectHostName: list.collectHostName,    //Not currently in use
						targetname: list.machineName,
						host: list.host,
						ots: list.ots,
						line: list.line,
						ftpUser: list.ftpUser,
						ftpPassword: list.ftpPassword,
						vftpUser: list.vftpUser,
						vftpPassword: list.vftpPassword,
						serialNumber: list.serialNumber,
						toolType: list.toolType,
						port: list.port,
						ftpConnected : list.ftpConnected,
						vftpConnected : list.vftpConnected,
						//targettype: list.machineType,             //Not currently in use
						checked: false
					}
				});
				const toolInfoVer = version;
				//console.log("newToolInfoList", newToolInfoList);
				return state.set('toolInfoList', fromJS(toolInfoList))
							.set('equipmentList', fromJS(equipList))
							.set('toolInfoVer',version);
			},
		}),

	...pender(
		{
			type: VIEW_UPDATE_MANUAL_TOOLINFO_LIST,
			onSuccess: (state, action) => {
				const { lists, version } = action.payload.data;
				const equipArray = Array.from(new Set(lists.map(item => item.line).filter(v => v !== null)));
				const equipList = equipArray.map((item, idx) => ({ keyIndex : idx, fabName: item}));
				const checkedList = state.get("toolInfoList").filter(item => item.get("checked"));
				const toolInfoDisplay = state.get("toolInfoDisplay");

				const toolInfoList = lists.map((list, idx) => {
					const connected = toolInfoDisplay === "vftp" ? list.vftpConnected : list.ftpConnected;
					return {
						keyIndex: idx,
						structId: list.line,
						//collectServerId: list.collectServerId,    //Not currently in use
						//collectHostName: list.collectHostName,    //Not currently in use
						targetname: list.machineName,
						host: list.host,
						ots: list.ots,
						line: list.line,
						ftpUser: list.ftpUser,
						ftpPassword: list.ftpPassword,
						vftpUser: list.vftpUser,
						vftpPassword: list.vftpPassword,
						serialNumber: list.serialNumber,
						toolType: list.toolType,
						port: list.port,
						ftpConnected: list.ftpConnected,
                        vftpConnected: list.vftpConnected,
						//targettype: list.machineType,             //Not currently in use
						checked: !!checkedList.find(item => item.get("targetname") === list.machineName) && connected
					}
				});
				return state.set('toolInfoList', fromJS(toolInfoList))
							.set('equipmentList', fromJS(equipList))
							.set('toolInfoVer',version);
			},
		}),

	...pender(
		{
			type: VIEW_UPDATE_AUTO_TOOLINFO_LIST,
			onSuccess: (state, action) => {
				const { lists, version } = action.payload.data;
				const equipArray = Array.from(new Set(lists.map(item => item.line).filter(v => v !== null)));
				const equipList = equipArray.map((item, idx) => ({ keyIndex : idx, fabName: item}));
				const checkedList = state.get("toolInfoList").filter(item => item.get("checked"));

				const toolInfoList = lists.map((list, idx) => {
					return {
						keyIndex: idx,
						structId: list.line,
						//collectServerId: list.collectServerId,    //Not currently in use
						//collectHostName: list.collectHostName,    //Not currently in use
						targetname: list.machineName,
						host: list.host,
						ots: list.ots,
						line: list.line,
						ftpUser: list.ftpUser,
						ftpPassword: list.ftpPassword,
						vftpUser: list.vftpUser,
						vftpPassword: list.vftpPassword,
						serialNumber: list.serialNumber,
						toolType: list.toolType,
						port: list.port,
						ftpConnected: list.ftpConnected,
						vftpConnected: list.vftpConnected,
						//targettype: list.machineType,             //Not currently in use
						checked: !!checkedList.find(item => item.get("targetname") === list.machineName)
					}
				});
				return state.set('toolInfoList', fromJS(toolInfoList))
							.set('equipmentList', fromJS(equipList))
							.set('toolInfoVer',version);
			},
		}),

	...pender({
		type: VIEW_LOAD_LOGTYPE_LIST,
		onSuccess: (state, action) => {
			console.log("handleActions[VIEW_LOAD_LOGTYPE_LIST]");
			const { lists ,version} = action.payload.data;
			//console.log("lists", lists);
			//console.log(lists[0].logType);
			const newLists = lists.map((lists, idx) => {
				return {
					keyIndex: idx,
					//logType: lists.categoryType,
					logCode: lists.categoryCode,
					logName: lists.categoryName,
					description: lists.description,
					dest:lists.dest,
					filePath: lists.filePath,
					fileName: lists.fileName,
					auto:  lists.auto,
					display:lists.display,

					//fileListForwarding: lists.fileListForwarding,   //Not currently in use
					checked: false
				}
			});

			//console.log("newLists", newLists);
			return state.set('logInfoList', fromJS(newLists))
						.set("logInfoVer",version);
		}
	}),

	[VIEW_INIT_ALL_LIST]: (state, action) => {
		return initialState;
	},

	[VIEW_SET_DISPLAY]: (state, action) => {
		return state.set('toolInfoDisplay', action.payload);
	},

	[VIEW_CHECK_TOOL_LIST]: (state, action) => {
		console.log("handleActions[VIEW_CHECK_TOOL_LIST]");
		const toolInfoList = state.get("toolInfoList");
		let toolInfoListCheckCnt = state.get("toolInfoListCheckCnt");
		const index = action.payload;

		//console.log("toolInfoList", toolInfoList);
		//console.log("index", index);

		const check =  toolInfoList.getIn([index, "checked"]);
		//console.log("check", check);
		if(check){
			toolInfoListCheckCnt--;
		} else {
			toolInfoListCheckCnt++;
		}

		return state.set("toolInfoList", toolInfoList.update(index, list => list.set("checked", !list.get("checked"))))
					.set("toolInfoListCheckCnt", toolInfoListCheckCnt);
	},

	[VIEW_CHECK_ALL_TOOL_LIST] : (state, action) => {
		const toolInfoList = state.get("toolInfoList");
		const { isAllCheck, isManual } = action.payload;
		let toolInfoListCheckCnt = 0;
		let newToolList;
		const toolInfoDisplay = state.get("toolInfoDisplay");

		if(isAllCheck){
			if(isManual) {
				if(toolInfoDisplay === "ftp")
					newToolList = toolInfoList.map(list => list.get("ots") && list.get("ftpConnected") ? list.set("checked", true) : list);
				else
					newToolList = toolInfoList.map(list => list.get("ots") && list.get("vftpConnected") ? list.set("checked", true) : list);

			} else {
				newToolList = toolInfoList.map(list => list.get("ots") ? list.set("checked", true) : list);
			}
			toolInfoListCheckCnt = newToolList.filter(list => list.get("checked")).size;
		} else {
			newToolList = toolInfoList.map(list => list.get("ots") ? list.set("checked", false) : list);
		}

		return state.set("toolInfoList", newToolList)
					.set("toolInfoListCheckCnt", toolInfoListCheckCnt);
	},

	[VIEW_CHECK_LOGTYPE_LIST]: (state, action) => {
		console.log("handleActions[VIEW_CHECK_LOGTYPE_LIST]");
		const logInfoList = state.get("logInfoList");
		let logInfoListCheckCnt = state.get("logInfoListCheckCnt");
		const index = action.payload;

		//console.log("logInfoList", logInfoList.toJS());
		//console.log("index", index);
		const check =  logInfoList.getIn([index, "checked"]);
		//console.log("check", check);
		if(check){
			logInfoListCheckCnt--;
		} else {
			logInfoListCheckCnt++;
		}

		return state.set("logInfoList", logInfoList.update(index, list => list.set("checked", !list.get("checked"))))
					.set("logInfoListCheckCnt", logInfoListCheckCnt);
	},

	[VIEW_CHECK_ALL_LOGTYPE_LIST] : (state, action) => {
		const logInfoList = state.get("logInfoList");
		const { check, actionList } = action.payload;
		let newLogInfoList = [];
		let newCnt = 0;

		if (typeof(actionList) === "undefined") {
			newLogInfoList = logInfoList.map(list => list.set("checked", false));
		} else {
			newLogInfoList = logInfoList.map(list => list.set("checked", actionList.includes(list.get("keyIndex")) ? check : list.get("checked")));
			newLogInfoList.map(list => { if (list.get("checked")) { newCnt++; }});
		}

		return state.set("logInfoList", newLogInfoList).set("logInfoListCheckCnt", newCnt);
	},

	[VIEW_APPLY_GENRE_LIST]: (state, action) => {
		console.log("handleActions[VIEW_APPLY_GENRE_LIST]");

		const logInfoList = state.get("logInfoList");
		const { genreList, id } = action.payload;

		// find selected genre list
		const selectedGenre =  genreList.get("list").find(item => {
			return item.get("id") == id;
		});

		let logInfoListCheckCnt = 0;
		// get category list
		const fileCat = selectedGenre.get("category");

		// all loginfo list init -> unchecked
		const initLogInfoList = logInfoList.map(list => list.set("checked", false));

		// check genre list
		const newLogInfoList = fileCat.reduce((pre, cur) => {
			const findList = initLogInfoList.find(item => item.get("logCode") == cur);
			return pre.update(findList.get("keyIndex"), list => {
				logInfoListCheckCnt++;
				return list.set("checked", true);});
		}, initLogInfoList);

		return state.set("logInfoList", newLogInfoList)
					.set("logInfoListCheckCnt", logInfoListCheckCnt);
	},
	[VIEW_SET_EDIT_PLAN_LIST] : (state, action) => {
		console.log("handleActions[VIEW_SET_EDIT_PLAN_LIST]");
		console.log("action.payload", action.payload);
		const { tool, logCode } = action.payload;
		//console.log("tool", tool);
		//console.log("logCode", logCode);

		const toolInfoList = state.get("toolInfoList");
		const logInfoList = state.get("logInfoList");
		let logInfoListCheckCnt = 0;
		let toolInfoListCheckCnt = 0;

		// all toolinfo list init -> unchecked
		const initToolInfoList = toolInfoList.map(list => list.set("checked", false));
		// check Edit list
		const newToolInfoList = tool.reduce((pre, cur) => {
			const findList = initToolInfoList.find(item => item.get("targetname") == cur);
			return pre.update(findList.get("keyIndex"), list => {
				toolInfoListCheckCnt++;
				return list.set("checked", true);});
		}, initToolInfoList);


		// all loginfo list init -> unchecked
		const initLogInfoList = logInfoList.map(list => list.set("checked", false));

		// check Edit list
		const newLogInfoList = logCode.reduce((pre, cur) => {
			const findList = initLogInfoList.find(item => item.get("logCode") == cur);
			return pre.update(findList.get("keyIndex"), list => {
				logInfoListCheckCnt++;
				return list.set("checked", true);});
		}, initLogInfoList);


		return state.set("logInfoList", newLogInfoList)
					.set("logInfoListCheckCnt", logInfoListCheckCnt)
					.set("toolInfoList", newToolInfoList)
					.set("toolInfoListCheckCnt", toolInfoListCheckCnt);
	}
}, initialState)