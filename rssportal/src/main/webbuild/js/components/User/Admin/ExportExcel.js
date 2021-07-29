import React, {useEffect, useState} from "react";

import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCloudDownloadAlt} from "@fortawesome/free-solid-svg-icons";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as viewListActions from "../../../modules/viewList";
import * as Define from "../../../define";
/*import xlsx from 'xlsx'*/
import {List, Map} from "immutable";
import { saveAs } from 'file-saver/FileSaver';
import {Workbook} from 'exceljs';
const ExportExcel = ({toolInfoList, logInfoList, toolInfoVer, logInfoVer, viewListActions}) => {
	const machineObject ={
		version: "",
		list: List([
			Map({
				no: 0,
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
	const categoryObject ={
		version: "",
		list: List([
			Map({
				no: 0,
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
	const excelDownload = async () => {
		await viewListActions.viewLoadLogTypeList(Define.REST_SYSTEM_GET_CATEGORIES);
		await viewListActions.viewLoadToolInfoList(Define.REST_SYSTEM_GET_MACHINES);
		const machineList = toolInfoList.toJS();
		const categoryList = logInfoList.toJS();
		categoryObject.version = logInfoVer;
		machineObject.version = toolInfoVer;
		machineObject.list = machineList.map((info) =>{
			return {
			no : info.keyIndex+1,
			type : (info.ots !=null) ?"MPA" : "OTS",
			targetname : info.targetname,
			host : info.host,
			port : info.port,
			ots : info.ots,
			line : info.line,
			ftpUser : info.ftpUser,
			ftpPassword : info.ftpPassword,
			vftpUser : info.vftpUser,
			vftpPassword : info.vftpPassword,
			serialNumber : info.serialNumber,
			toolType : info.toolType,
			}
		})
		categoryObject.list =categoryList.map((info) =>{
			return {
				no : parseInt(info.logCode,16),
				logName : info.logName,
				description : info.description,
				dest : (info.dest =="Cons") ?"Cons" : "Logsv",
				filePath : info.filePath,
				fileName : info.fileName,
				auto  : (info.auto)?"TRUE" :"FALSE",
				display : (info.display)?"TRUE" :"FALSE",
			}
		})
		const workbook = new Workbook();
		workbook.created = new Date();
		const machineSheet = workbook.addWorksheet('Machine',{views:[{state: 'frozen', ySplit: 4}]});
		const categorySheet = workbook.addWorksheet('Category',{views:[{state: 'frozen', ySplit: 3}]});
		const cA1= categorySheet.addRow(["Version: "+ categoryObject.version]);
		const mA1= machineSheet.addRow(["Version: "+ machineObject.version]);
		mA1.font = cA1.font ={ name: 'MS PGothic', size: 11, color: { argb: '00000000',}, bold: false};
		mA1.fill = cA1.fill ={type: 'pattern', pattern: 'solid', fgColor: {argb: 'FFFFE4B5'}}
		mA1.alignment = cA1.alignment = { vertical: 'middle'};
		mA1.height = cA1.height = 24;
		machineSheet.mergeCells('A1:N1');
		categorySheet.mergeCells('A1:I1');
		machineSheet.addRow([]);
		categorySheet.addRow([]);

		const machineTitle1=machineSheet.addRow([]);
		machineSheet.getCell('B3').value = "No.";
		machineSheet.getCell('C3').value = "Machine\nType";
		machineSheet.getCell('D3').value = "Common";
		machineSheet.getCell('G3').value = "MPA";
		const machineTitle2= machineSheet.addRow(['' ,'', '', 'Name', 'Host', 'Port', 'OTS', 'Line', 'Ftp User ID', 'Ftp User PW',
			'vFtp User ID', 'vFtp User PW', 'Serial Number', 'Tool Type']);
		machineTitle2.height = 23.5;
		machineTitle1.eachCell((cell,index) => {
			if(index>1){
				cell.border = {top: {style: 'medium'}, left: {style: 'medium'}, bottom: {style: 'thin'}, right: {style: 'medium'}},
					cell.font = { name: 'MS PGothic', size: 11, color: { argb: '00000000'}, bold: false};
				cell.alignment = { vertical: 'middle', horizontal: 'center' ,wrapText: true };
				cell.fill ={type: 'pattern', pattern: 'solid', fgColor: {argb: 'FFE6E6FA'}}
			}
		});
		machineTitle2.eachCell((cell,index) => {
			if(index>1){
				cell.border = {top: {style: 'thin'}, left: ((index==2 ||index==3||index==4||index==7))?{style: 'medium'}:{style: 'thin'}, bottom: {style: 'thin'}, right: ((index==2 ||index==3||index==6||index==14))?{style: 'medium'}:{style: 'thin'}};
				cell.font = { name: 'MS PGothic', size: 11, color: { argb: '00000000'}, bold: false};
				cell.alignment = { vertical: 'middle', horizontal: 'center' , wrapText: true };
				cell.fill ={type: 'pattern', pattern: 'solid', fgColor: {argb: 'FFE6E6FA'}}
			}
		});
		machineSheet.mergeCells('B3:B4');
		machineSheet.mergeCells('C3:C4');
		machineSheet.mergeCells('D3:F3');
		machineSheet.mergeCells('G3:N3');
		machineSheet.columns = [
			{ key: ''},
			{ key: 'No'},
			{ key: 'Type'},
			{ key: 'Name'},
			{ key: 'Host'},
			{ key: 'Port'},
			{ key: 'OTS'},
			{ key: 'Line'},
			{ key: 'FtpUser'},
			{ key: 'FtpPassword'},
			{ key: 'vFtpUser'},
			{ key: 'vFtpPassword'},
			{ key: 'SerialNumber'},
			{ key: 'ToolType'},
		];
		machineObject.list.forEach(function(item) {
			const row = machineSheet.addRow({
				No: item.no,
				Type: item.type,
				Name: item.targetname,
				Host:item.host,
				Port: item.port?item.port:'',
				OTS: item.ots?item.ots:'',
				Line:item.line? item.line:'',
				FtpUser: item.ftpUser?item.ftpUser:'',
				FtpPassword: item.ftpPassword?item.ftpPassword:'',
				vFtpUser: item.vftpUser?item.vftpUser:'',
				vFtpPassword: item.vftpPassword?item.vftpPassword:'',
				SerialNumber: item.serialNumber?item.serialNumber:'',
				ToolType: item.toolType?item.toolType:'',
			})
			row.fill = {
				type: 'pattern',
				pattern: 'solid',
				fgColor: {
					argb: 'FFFFFFFF'
				}
			};
			row.alignment ={vertical: 'middle', horizontal: 'center'};
			row.font = {
				name: 'MS PGothic',
				size: 11,
				color: {
					argb: '00000000',
				},
				bold: false
			}
			row.eachCell((cell,index) => {
				cell.border = {
					top: {style: 'thin'},
					left: ((index==2 ||index==3||index==4||index==7))?{style: 'medium'}:{style: 'thin'},
					bottom: {style: 'thin'},
					right: ((index==2 ||index==3||index==6||index==14))?{style: 'medium'}:{style: 'thin'}};
/*
				cell.border = {top: {style: 'thin'}, left: {style: 'thin'}, bottom: {style: 'thin'}, right: {style: 'thin'}};
*/
			});
		});
		machineSheet.getColumn(1).width = 2;
		machineSheet.getColumn(2).width = 4.5;
		machineSheet.getColumn(3).width = 8.5;
		machineSheet.getColumn(4).width = 8.5;
		machineSheet.getColumn(5).width = 15.6;
		machineSheet.getColumn(6).width = 8.5;
		machineSheet.getColumn(7).width = 8.5;
		machineSheet.getColumn(8).width = 13;
		machineSheet.getColumn(9).width = 13;
		machineSheet.getColumn(10).width = 13;
		machineSheet.getColumn(11).width = 13;
		machineSheet.getColumn(12).width = 13;
		machineSheet.getColumn(13).width = 14.5;
		machineSheet.getColumn(14).width = 10;


		const categoryTitle= categorySheet.addRow(['' ,'No.', 'CategoryName\n(Max: 50Byte)', 'Description\n(Max: 150Byte)', 'Dest',
			'FilePath\n(Max: 150Byte)', 'FileName\n(Max: 50Byte)', 'Auto\nCollect', 'Display']);
		categoryTitle.height = 44;
		categoryTitle.eachCell((cell,index) => {
			if(index>1){
				cell.border = {top: {style: 'medium'}, left: (index ==2 )?{style: 'medium'}:{style: 'thin'}, bottom: {style: 'thin'}, right: (index ==9 )?{style: 'medium'}:{style: 'thin'}},
				cell.font = { name: 'MS PGothic', size: 11, color: { argb: '00000000'}, bold: true};
				cell.alignment = { vertical: 'middle', horizontal: 'center' ,wrapText: true  };
				cell.fill ={type: 'pattern', pattern: 'solid', fgColor: {argb: 'FFE6E6FA'}}
			}
		});
		categorySheet.columns = [
			{ key: ''},
			{ key: 'No'},
			{ key: 'logName'},
			{ key: 'Description'},
			{ key: 'Dest'},
			{ key: 'FilePath'},
			{ key: 'FileName'},
			{ key: 'Auto'},
			{ key: 'Display'}
		];
		categoryObject.list.forEach(function(item) {
			const row = categorySheet.addRow({
				No: item.no,
				logName: item.logName,
				Description: item.description?item.description:'',
				Dest:item.dest,
				FilePath: item.filePath,
				FileName: item.fileName,
				Auto: item.auto,
				Display: item.display,
			})
			row.fill = {
				type: 'pattern',
				pattern: 'solid',
				fgColor: {
					argb: 'FFFFFFFF'
				}
			};
			row.font = {
				name: 'MS PGothic',
				size: 11,
				color: {
					argb: '00000000',
				},
				bold: false
			}
			row.eachCell((cell) => {
				cell.border = {
					top: {style: 'thin'},
					left: {style: 'thin'},
					bottom: {style: 'thin'},
					right: {style: 'thin'}};
			});
		});

		categorySheet.getColumn(1).width = 2;
		categorySheet.getColumn(2).width = 4.5;
		categorySheet.getColumn(3).width = 55;
		categorySheet.getColumn(4).width = 27;
		categorySheet.getColumn(5).width = 13;
		categorySheet.getColumn(6).width = 86;
		categorySheet.getColumn(7).width = 31.5;
		categorySheet.getColumn(8).width = 12;
		categorySheet.getColumn(9).width = 8.5;
		workbook.xlsx.writeBuffer().then((data) => {
			const blob = new Blob([data], {
				type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
			});
			saveAs(blob, 'Configuration.xlsx');
		});
	}

	return (<button className="control-btn" onClick={()=>excelDownload()}>
						 <span><FontAwesomeIcon icon={faCloudDownloadAlt} size="lg"/></span>
						 <span>export</span></button>
	);
};


export default connect(
	(state) => ({
		toolInfoList: state.viewList.get('toolInfoList'),
		logInfoList: state.viewList.get('logInfoList'),
		toolInfoVer: state.viewList.get('toolInfoVer'),
		logInfoVer: state.viewList.get('logInfoVer'),
	}),
	(dispatch) => ({
		viewListActions: bindActionCreators(viewListActions, dispatch),
	}),
)(ExportExcel);
