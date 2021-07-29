import React, {useState} from "react";
import ReactTransitionGroup from "react-addons-css-transition-group";
import { faCheckCircle, faExclamationCircle } from "@fortawesome/free-solid-svg-icons";
import {CustomInput, FormGroup} from "reactstrap";
import xlsx from 'xlsx';
import {List, Map} from "immutable";
import services from "../../../services";
import * as Define from "../../../define";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as viewListActions from "../../../modules/viewList";
import ModalTooltip from "./ModalTooltip";
import {stringBytes} from "../../Common/CommonFunction";

const ImportModal = React.memo(({isOpen, close, viewListActions, alertOpen}) => {
	const [uploadFileName, setUploadFileName] = useState(false);
	const [isError, setIsError] = useState(false);
	const categoryObject ={
		version: "",
		list: List([
			Map({
				No: 0,
				logName: "",
				description: "",
				dest:"",
				filePath: "",
				fileName: "",
				auto: true,
				display: true,
			})
		]),
	};
	const machineObject ={
		version: "",
		list: List([
			Map({
				No: 0,
				type:"",
				targetname: "",
				host: "",
				ots:"",
				ftpUser: "",
				ftpPassword: "",
				vftpUser: "",
				vftpPassword: "",
				serialNumber: "",
				toolType: "",
				port:"",
			})
		]),
	};
/******************COMMON FUNCTION******************************************************************************************/
	const msgList = {
		MSG_1: "Please check the Category's required fields.",
		MSG_2: "Please check the maximum input length of each field",
		MSG_3: "Please check the Category's No.(using the duplicated)",
		MSG_4: "Have finished importing the Categories.",
		MSG_5: "Please check the Machine's required fields.",
		MSG_6: "Please check the Machine's Name.(using the duplicated)",
		MSG_7: "Have finished importing the machines.",
		MSG_8: "Have finished importing the machines and the categories",
		MSG_9: "There are no machines and categories to import."
	};
	const convertJson = (info) => {
		const jsonData ={
			version: "",
			list: "",
		};
		const formulae = xlsx.utils.sheet_to_formulae(info);
		jsonData.version = formulae[0].substring((formulae[0].indexOf(": ")) ? formulae[0].indexOf(": ")+1 : formulae[0].indexOf(":")+1,formulae[0].length).replace(/^\s+|\s+$/gm,'');
		jsonData.list = xlsx.utils.sheet_to_json(info);
		return jsonData;
	}
	const input_length_check = (a,b) => {return (a !== undefined) ? stringBytes(a.toString()) <= b : true };
	const input_null_check = (a) => {
		if(a === undefined) return true;
		const str = a.toString().replace(/^\s+|\s+$/,'');
		return str === null || str === "";
	};

	const is_number=(v)=> {
		if(v === undefined) return false;
		const reg = /^(\s|\d)+$/;
		console.log("[is_number] ",reg.test(v));
		return reg.test(v);
	}


/******************MACHINE FUNCTION******************************************************************************************/

	const MachineVerification= (lists) =>{
		const ret ={
			result: true,
			msg: 0
		}
		const maxLen ={
			targetname: 25,
			host: 50,
			ots:25,
			line:25,
			ftpUser: 20,
			ftpPassword: 20,
			vftpUser: 20,
			vftpPassword: 20,
			serialNumber: 25,
			toolType: 25,
			port:6,
		}
		console.log("==========MachineVerification==========");
		const notNullCheck= lists.filter(info => {
			return info.type ==="OTS" || info.type ==="" && input_null_check(info.ots)
				? (input_null_check(info.targetname)|| input_null_check(info.host)||info.port === 0)
				: (input_null_check(info.targetname)|| input_null_check(info.host)|| input_null_check(info.ots)||input_null_check(info.line))});
		console.log("notNullCheck: ",notNullCheck);
		if(notNullCheck !==undefined && notNullCheck.length > 0 )
		{
			ret.result = false;
			ret.msg = msgList.MSG_5;
			return ret;
		}
		const maxLengthCheck =
			lists.filter(info => { return info.type ==="OTS" || info.type ==="" && input_null_check(info.ots)
				? (!input_length_check(info.targetname,maxLen.targetname) || !input_length_check(info.host,maxLen.host)
					|| !input_length_check(is_number(info.port) ? info.port.toString(): info.port,maxLen.port))
				:(!input_length_check(info.targetname,maxLen.targetname) || !input_length_check(info.host,maxLen.host)
					|| !input_length_check(info.ots,maxLen.ots)|| !input_length_check(info.line,maxLen.line))});
		console.log("maxLengthCheck: ",maxLengthCheck);
		if(maxLengthCheck !==undefined && maxLengthCheck.length > 0 )
		{
			ret.result = false;
			ret.msg = msgList.MSG_2;
			return ret;
		}
		const duplicatedName= lists.map((info) => {return info.targetname}).find((obj,i,arr) => {return arr.indexOf(obj) !== i});
		console.log("duplicatedNo: ",duplicatedName);
		if(duplicatedName !== undefined)
		{
			ret.result = false;
			ret.msg =  msgList.MSG_6;
			return ret;
		}
		console.log("==========MachineVerification==========");
		return ret;

	}
/******************CATEGORY FUNCTION******************************************************************************************/
	const CategoryVerification= (lists) =>{
		const ret ={
			result: true,
			msg: 0
		}
		const maxLen ={
			No:3,
			logName: 50,
			dest:4,
			description:200,
			filePath:150,
			fileName:50,
			auto:5,
			display:5
		}
		console.log("==========CategoryVerification==========");
		const notNullCheck= lists.filter(info => {
			return (info.No === 0 || info.dest ==="" ||info.auto ==="" ||info.display ===""
					||input_null_check(info.logName) ||input_null_check(info.filePath ) ||input_null_check(info.fileName ))});
		console.log("notNullCheck: ",notNullCheck);
		if(notNullCheck !==undefined && notNullCheck.length > 0 )
		{
			ret.result = false;
			ret.msg = msgList.MSG_1;
			return ret;
		}
		const maxLengthCheck =
			lists.filter(info => { return !input_length_check(info.No.toString(),maxLen.No) ||
				!input_length_check(info.logName,maxLen.logName) || !input_length_check(info.description,maxLen.description)
				|| !input_length_check(info.filePath,maxLen.filePath) || !input_length_check(info.fileName,maxLen.fileName)
				|| (info.dest.toString() !=="Cons" && info.dest.toString() !=="Logsv")
				|| (info.auto.toString() !=="TRUE" && info.auto.toString() !=="FALSE")
				|| (info.display.toString() !=="TRUE" && info.display.toString() !=="FALSE")});
		console.log("maxLengthCheck: ",maxLengthCheck);
		if(maxLengthCheck !==undefined && maxLengthCheck.length > 0 )
		{
			ret.result = false;
			ret.msg = msgList.MSG_2;
			return ret;
		}
		const duplicatedNo = lists.map((info) => {return info.No}).find((obj,i,arr) => {return arr.indexOf(obj) !== i});
		console.log("duplicatedNo: ",duplicatedNo);
		if(duplicatedNo !== undefined)
		{
			ret.result = false;
			ret.msg =  msgList.MSG_3;
			return ret;
		}
		console.log("==========CategoryVerification==========");
		return ret;
	}


	const HandleImportButton = () => {
		if (uploadFileName !== false) {
			const reader = new FileReader();
			reader.onload = async () => {
				console.log("============reader.onload=======");
				const data = reader.result;
				const workbook = xlsx.read(data, {type: 'binary'});
				const sheetNameList = workbook.SheetNames; //
				const cSheet = sheetNameList.findIndex(item => item.toLowerCase() === "category");
				const mSheet = sheetNameList.findIndex(item => item.toLowerCase() === "machine");
				if (cSheet != '-1') {
					const sheet = workbook.Sheets[sheetNameList[cSheet]];
					const cData = convertJson(sheet);
					categoryObject.version = cData.version;
					categoryObject.list = cData.list.map(
						(obj) => {
							return {
								No: (Number.isNaN(obj['__EMPTY']) || obj['__EMPTY'] === undefined || obj['__EMPTY'] === "") ? 0 : obj['__EMPTY'],
								logName: obj['__EMPTY_1'] ? obj['__EMPTY_1'].toString() : "",
								description: obj['__EMPTY_2'] ? obj['__EMPTY_2'].toString() : "",
								dest: (obj['__EMPTY_3'] == undefined || obj['__EMPTY_3'] == null) ?
									"" : obj['__EMPTY_3'].toString().toUpperCase() ==="CONS" ? "Cons" : obj['__EMPTY_3'].toString().toUpperCase() ==="LOGSV" ? "Logsv":"",
								filePath: obj['__EMPTY_4'] ? obj['__EMPTY_4'].toString() : "",
								fileName: obj['__EMPTY_5'] ? obj['__EMPTY_5'].toString() : "",
								auto: (obj['__EMPTY_6'] == undefined || obj['__EMPTY_6'] == null) ? "" : obj['__EMPTY_6'].toString().toUpperCase(),
								display: (obj['__EMPTY_7'] == undefined || obj['__EMPTY_7'] == null) ? "" : obj['__EMPTY_7'].toString().toUpperCase()
							}
						}).filter((info, idx) => {
						return idx > 0
					});
					console.log(categoryObject);
				}
				if (mSheet != '-1') {
					const sheet = workbook.Sheets[sheetNameList[mSheet]];
					const mData = convertJson(sheet);
					machineObject.version = mData.version;
					machineObject.list = mData.list.map(
						(obj) => {
							return {
								type: (obj['__EMPTY_1'] == undefined || obj['__EMPTY_1'] == null) ? "" : obj['__EMPTY_1'].toString().toUpperCase(),
								targetname: obj['__EMPTY_2'].toString(),
								host: (obj['__EMPTY_3'] == undefined || obj['__EMPTY_3'] == null) ? "" : obj['__EMPTY_3'].toString(),
								port: (Number.isNaN(obj['__EMPTY_4']) || obj['__EMPTY_4'] == "" || obj['__EMPTY_4'] == undefined)? 0: obj['__EMPTY_4'],
								ots: obj['__EMPTY_5'] ? obj['__EMPTY_5'].toString() : "",
								line: obj['__EMPTY_6'] ?  obj['__EMPTY_6'].toString() : "",
								ftpUser: obj['__EMPTY_7'] ?  obj['__EMPTY_7'].toString() : "",
								ftpPassword: obj['__EMPTY_8'] ?  obj['__EMPTY_8'].toString() : "",
								vftpUser: obj['__EMPTY_9'] ?  obj['__EMPTY_9'].toString() : "",
								vftpPassword: obj['__EMPTY_10'] ?  obj['__EMPTY_10'].toString() : "",
								serialNumber: obj['__EMPTY_11'] ?  obj['__EMPTY_11'].toString() : "",
								toolType: obj['__EMPTY_12'] ?  obj['__EMPTY_12'].toString() : ""}
						}).filter((info,idx )=> {
							return (idx>1)&&(info.targetname !== undefined)
						});
					console.log("machineObject");
					console.log(machineObject);
				}
				if(cSheet != '-1' ||  mSheet != '-1')
				{
					let cVerfication = (cSheet != '-1') ? CategoryVerification(categoryObject.list) : {result: false,msg:0};
					let mVerification = (mSheet != '-1') ?MachineVerification(machineObject.list) : {result: false,msg:0};
					console.log("CategoryVerification",cVerfication);
					console.log("MachineVerification",mVerification);
					closeModal();
					if(cVerfication.msg ) { setTimeout(() => { alertOpen(cVerfication.msg, faExclamationCircle); }, 400);}
					else if(mVerification.msg ){ setTimeout(() => { alertOpen(mVerification.msg, faExclamationCircle); }, 400);}
					else{
						if(cVerfication.result && !mVerification.result) {
							setTimeout(async () => {
								alertOpen(msgList.MSG_4, faCheckCircle);
								await refreshPage(mVerification.result,cVerfication.result);
							},400);
						} else if (mVerification.result&& !cVerfication.result) {
							setTimeout(async () => {
								alertOpen(msgList.MSG_7, faCheckCircle);
								await refreshPage(mVerification.result,cVerfication.result);
							}, 400);
						}	else {
							setTimeout(async () => {
								alertOpen(msgList.MSG_8, faCheckCircle);
								await refreshPage(mVerification.result,cVerfication.result);
							}, 400);
						}
					}
				}else {
					closeModal();
					setTimeout(() => { alertOpen(msgList.MSG_9, faExclamationCircle); }, 400);
				}
			}
			console.log("============reader.readAsBinaryString=======");
			reader.readAsBinaryString(uploadFileName);

		} else {
			setIsError(true);
		}
	}

	const HandleFileInput = (e) => {
		const { files } = e.target;
		if (files.length > 0) {
			const fileName = files[0].name;
			const fileExtension = fileName.substring(fileName.indexOf("."));

			if (fileName === "" || (fileExtension !== ".xls" && fileExtension !== ".xlsx")) {
				setIsError(true);
			} else {
				setIsError(false);
				setUploadFileName(files[0]);
			}
		} else {
			setIsError(true);
		}
	}

	const closeModal = () => {
		setIsError(false);
		setUploadFileName(false);
		close();
	}

	const refreshPage = async (m,c) => {
		if(c) {
			await services.axiosAPI.requestPost(Define.REST_SYSTEM_IMPORT_CATEGORIES, categoryObject).then(r => r).catch((e) => {
				console.log(e)});
			await viewListActions.viewLoadLogTypeList(Define.REST_SYSTEM_GET_CATEGORIES);
		}
		if(m) {
			await services.axiosAPI.requestPost(Define.REST_SYSTEM_IMPORT_MACHINES, machineObject).then(r => r).catch((e) => {
				console.log(e)});
			await viewListActions.viewLoadToolInfoList(Define.REST_SYSTEM_GET_MACHINES);
		}
	}

	return (
		<>
			{isOpen ? (
				<ReactTransitionGroup
					transitionName={"Custom-modal-anim"}
					transitionEnterTimeout={200}
					transitionLeaveTimeout={200}
				>
					<div className="Custom-modal-overlay" onClick={closeModal} />
					<div className="Custom-modal">
						<p className="title">System Configuration Import</p>
						<div className="content-with-title system-setting">
							<FormGroup>
								<CustomInput id="importFile" type="file" onChange={(e) => HandleFileInput(e)}/>
								<span className={"error" + (isError ? " active" : "")}>File is invalid.</span>
								<ModalTooltip
									target="importFile"
									header="File Import"
									body="Only files with a .xls/.xlsx file extension can be imported."
									placement="right"
									trigger="hover" />
							</FormGroup>
						</div>
						<div className="button-wrap no-margin">
							<button	className="form-type left-btn system-setting"	onClick={() =>HandleImportButton()}>
								Apply
							</button>
							<button className="form-type right-btn system-setting" onClick={closeModal}>
								Cancel
							</button>
						</div>
					</div>
				</ReactTransitionGroup>
			) : (
				<ReactTransitionGroup
					transitionName={"Custom-modal-anim"}
					transitionEnterTimeout={200}
					transitionLeaveTimeout={200}
				/>
			)}
		</>
	);
});
export default connect(
	(state) => ({
		toolInfoList: state.viewList.get('toolInfoList'),
		toolInfoVer: state.viewList.get('toolInfoVer'),
		logInfoList: state.viewList.get('logInfoList'),
		logInfoVer: state.viewList.get('logInfoVer'),
	}),
	(dispatch) => ({
		viewListActions: bindActionCreators(viewListActions, dispatch),
	}),
)(ImportModal);
