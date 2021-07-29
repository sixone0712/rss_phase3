import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import {fromJS, List, Map} from "immutable";
import sinon from "sinon";
import ExportExcel from "../ExportExcel";
import {initialState as viewListInit} from "../../../../modules/viewList";
import produce from "immer";

const machineList = List([
	Map({
		keyIndex: 0,
		structId: "",
		targetname: "OTS 01",
		host: "xxx.xxx.xxx.xxx",
		ots: null,
		line: "",
		serialNumber: "",
		port: 80,
		ftpUser: "",
		ftpPassword: "",
		vftpUser: "",
		vftpPassword: "",
		toolType: "",
		checked: false
	}),
	Map({
		keyIndex: 1,
		structId: "Fab 1",
		targetname: "MPA 01",
		host: "xxx.xxx.xxx.xxx",
		ots: "OTS 01",
		line: "Fab 1",
		serialNumber: "MPA 01",
		port: 0,
		ftpUser: "ckbs",
		ftpPassword: "ckbs",
		vftpUser: "ckbs",
		vftpPassword: "ckbs",
		toolType: "xxx",
		checked: false
	}),
	Map({
		keyIndex: 2,
		structId: "Fab 1",
		targetname: "MPA 02",
		host: "xxx.xxx.xxx.xxx",
		ots: "OTS 01",
		line: "Fab 1",
		serialNumber: "MPA 02",
		port: 0,
		ftpUser: "ckbs",
		ftpPassword: "ckbs",
		vftpUser: "ckbs",
		vftpPassword: "ckbs",
		toolType: "xxx",
		checked: false
	})
]);

const logList = List([
	Map(
		{
			keyIndex: 0,
			logCode: "001",
			logName: "test code 001",
			description: "test purpose",
			dest: "Logsv",
			filePath: "/jest/test",
			fileName: "result.html",
			auto: false,
			display: false
		}
	),
	Map(
		{
			keyIndex: 1,
			logCode: "002",
			logName: "test code 002",
			description: "test purpose",
			dest: "Cons",
			filePath: "/jest/test",
			fileName: "result.html",
			auto: true,
			display: true
		}
	),
]);

const mockStore = configureStore();
const dispatch = sinon.spy();

describe('ExportExcel', () => {
	const defaultStore = mockStore({ viewList: viewListInit });
	const fullStore =
		mockStore(
			{ viewList:
				fromJS(produce(viewListInit.toJS(),
					draft => {
						draft.toolInfoList = machineList;
						draft.logInfoList = logList;
					}
				))
			}
		);

	it('renders correctly', async () => {
		const wrapper = shallow(<ExportExcel dispatch={dispatch} store={defaultStore} />).dive().dive();
		await wrapper.find("button").simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(full data)', async () => {
		const wrapper = shallow(<ExportExcel dispatch={dispatch} store={fullStore} />).dive().dive();
		await wrapper.find("button").simulate("click");
		expect(wrapper).toMatchSnapshot();
	});
});

