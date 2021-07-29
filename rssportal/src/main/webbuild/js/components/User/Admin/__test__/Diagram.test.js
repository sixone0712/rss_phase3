import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import { fromJS } from "immutable";
import sinon from "sinon";
import * as Define from "../../../../define";
import services from '../../../../services';
import Diagram from "../Diagram";
import {initialState as viewListInit} from "../../../../modules/viewList";
import produce from "immer";

const initialState = {
	diagramModalOpen: false,
	alertOpen: false,
	alertMsg: null,
	sMachine: 0,
};

const fullMachineList = [
	{
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
	},
	{
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
	},
	{
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
	}
];

const exceptMpaMachineList = [
	{
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
	}
];

const passwordNotSetMachineList = [
	{
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
	},
	{
		keyIndex: 1,
		structId: "Fab 1",
		targetname: "MPA 01",
		host: "xxx.xxx.xxx.xxx",
		ots: "OTS 01",
		line: "Fab 1",
		serialNumber: "MPA 01",
		port: 0,
		ftpUser: "ckbs",
		ftpPassword: "",
		vftpUser: "ckbs",
		vftpPassword: "",
		toolType: "xxx",
		checked: false
	}
];

const mockStore = configureStore();
const dispatch = sinon.spy();

services.axiosAPI.requestGet = jest.fn().mockResolvedValue({ status: Define.OK });
services.axiosAPI.requestPost = jest.fn().mockResolvedValue({ status: Define.OK });
services.axiosAPI.requestPatch = jest.fn().mockResolvedValue({ status: Define.OK });

describe('Diagram', () => {
	const defaultStore = mockStore({ viewList: viewListInit });
	const fullStore = mockStore({ viewList: fromJS(produce(viewListInit.toJS(), draft => { draft.toolInfoList = fullMachineList; })) });
	const exceptMpaStore = mockStore({ viewList: fromJS(produce(viewListInit.toJS(), draft => { draft.toolInfoList = exceptMpaMachineList; })) });
	const passwordNotSetStore = mockStore({ viewList: fromJS(produce(viewListInit.toJS(), draft => { draft.toolInfoList = passwordNotSetMachineList; })) });

	it('renders correctly', () => {
		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={defaultStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		wrapper.children().find("Memo()").at(2).dive();
		expect(wrapper).toMatchSnapshot();
	});

	it('open add modal(normal case)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {
					lists: [
						{
							ots: "OTS 01",
							machineNames: "MPA 01",
							machineName: "OTS 01",
							detailedStatus: "completed"
						}
					]
				}
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		await wrapper.children().find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it('open add modal(machine is working)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {
					lists: [
						{
							ots: "OTS 01",
							machineNames: "MPA 01",
							machineName: "OTS 01"
						}
					]
				}
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		await wrapper.children().find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it('close add modal', () => {
		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={defaultStore}
				initialState={{...initialState, diagramModalOpen: true}}
				openDelete={jest.fn()}/>).dive().dive();
		wrapper.children().find("Memo()").at(0).props().close();
		expect(wrapper).toMatchSnapshot();
	});

	it('add new mpa(normal case)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
{
				status: Define.OK,
				data: {
					lists: [
						{
							ots: "OTS 01",
							machineNames: "MPA 01",
							machineName: "OTS 01"
						}
					]
				}
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={exceptMpaStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		await wrapper.children().find("Memo()").at(0).props().apply(
			{
				host : "1.1.1.1",
				line : "Fab 1",
				ots : "OTS 01",
				ftpPassword : "test",
				ftpUser : "test",
				vftpPassword: "test",
				vftpUser: "test",
				serialNumber : "1234",
				structId: "test 01",
				targetname: "MPA 10",
				toolType: "test"
			}, 0);
		expect(wrapper).toMatchSnapshot();
	});

	it('add new mpa(ots is null)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {
					lists: [
						{
							ots: null,
							machineNames: "MPA 01",
							machineName: ""
						}
					]
				}
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={exceptMpaStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		jest.useFakeTimers();
		await wrapper.children().find("Memo()").at(0).props().apply({
			host : "1.1.1.1",
			line : "Fab 1",
			ots : "OTS 01",
			ftpPassword : "test",
			ftpUser : "test",
			vftpPassword: "test",
			vftpUser: "test",
			serialNumber : "1234",
			structId: "test 01",
			targetname: "MPA 10",
			toolType: "test"
		}, 0);
		jest.advanceTimersByTime(1000);
		jest.useRealTimers();
		expect(wrapper).toMatchSnapshot();
	});

	it('add new mpa(name is duplicate)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {
					lists: [
						{
							ots: null,
							machineNames: "",
							machineName: "MPA 01"
						}
					]
				}
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={exceptMpaStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		await wrapper.children().find("Memo()").at(0).props().apply({
			host : "1.1.1.1",
			line : "Fab 1",
			ots : "OTS 01",
			ftpPassword : "test",
			ftpUser : "test",
			vftpPassword: "test",
			vftpUser: "test",
			serialNumber : "1234",
			structId: "test 01",
			targetname: "MPA 01",
			toolType: "test"
		}, 0);
		expect(wrapper).toMatchSnapshot();
	});

	it('add new ots(normal case)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {
					lists: [
						{
							ots: null,
							machineNames: "",
							machineName: "MPA 01"
						}
					]
				}
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={exceptMpaStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		await wrapper.children().find("Memo()").at(0).props().apply({
			ots: null,
			host: "1.1.1.1",
			targetname: "OTS 10",
			port: "80"
		}, 0);
		expect(wrapper).toMatchSnapshot();
	});

	it('edit mpa(normal case)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {
					lists: [
						{
							ots: "OTS 01",
							machineNames: "MPA 01",
							machineName: "OTS 01"
						}
					]
				}
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		await wrapper.children().find("Memo()").at(0).props().apply(
			{
				host : "1.1.1.1",
				line : "Fab 1",
				ots : "OTS 01",
				ftpPassword : "test",
				ftpUser : "test",
				vftpPassword: "test",
				vftpUser: "test",
				serialNumber : "1234",
				structId: "test 01",
				targetname: "MPA 10",
				toolType: "test"
			}, 1);
		expect(wrapper).toMatchSnapshot();
	});

	it('edit mpa(mpa is working)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {
					lists: [
						{
							ots: "OTS 01",
							machineNames: "MPA 01",
							machineName: "OTS 01",
							detailedStatus: "collecting"
						}
					]
				}
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		await wrapper.children().find("Memo()").at(0).props().apply(
			{
				host : "1.1.1.1",
				line : "Fab 1",
				ots : "OTS 01",
				ftpPassword : "test",
				ftpUser : "test",
				vftpPassword: "test",
				vftpUser: "test",
				serialNumber : "1234",
				structId: "test 01",
				targetname: "MPA 01",
				toolType: "test"
			}, 1);
		expect(wrapper).toMatchSnapshot();
	});

	it('edit mpa(ots was deleted)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {
					lists: [
						{
							machineNames: "MPA 10",
							machineName: null,
							detailedStatus: "completed"
						}
					]
				}
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		await wrapper.children().find("Memo()").at(0).props().apply(
			{
				host : "1.1.1.1",
				line : "Fab 1",
				ots : "OTS 01",
				ftpPassword : "test",
				ftpUser : "test",
				vftpPassword: "test",
				vftpUser: "test",
				serialNumber : "1234",
				structId: "test 01",
				targetname: "MPA 01",
				toolType: "test"
			}, 1);
		expect(wrapper).toMatchSnapshot();
	});

	it('edit ots(normal case)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {	lists: [{	machineNames: "MPA 01" }] }
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		await wrapper.children().find("Memo()").at(0).props().apply(
			{
				host : "1.1.1.1",
				targetname: "OTS 01",
				port: "80"
			}, 1);
		expect(wrapper).toMatchSnapshot();
	});

	it('edit ots(mpa is not exist)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {	lists: [{	machineNames: "MPA 01" }] }
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={exceptMpaStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		await wrapper.children().find("Memo()").at(0).props().apply(
			{
				host : "1.1.1.1",
				targetname: "OTS 01",
				port: "80"
			}, 1);
		expect(wrapper).toMatchSnapshot();
	});

	it('close alert modal(redraw diagram modal case 1)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {	lists: [{	machineNames: "MPA 01" }] }
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={fullStore}
				initialState={{...initialState, alertMsg: "The name is already registered. Please register again."}}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		await wrapper.children().find("Memo()").at(1).props().closer();
		expect(wrapper).toMatchSnapshot();
	});

	it('close alert modal(redraw diagram modal case 2)', async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue(
			{
				status: Define.OK,
				data: {	lists: [{	machineNames: "MPA 01" }] }
			});

		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={fullStore}
				initialState={{...initialState, alertMsg: "MPA cannot be added because OTS has been deleted."}}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(0).props());
		jest.useFakeTimers();
		await wrapper.children().find("Memo()").at(1).props().closer();
		jest.advanceTimersByTime(1000);
		jest.useRealTimers();
		expect(wrapper).toMatchSnapshot();
	});

	it('render MachineList(OTS/MPA are exist)', () => {
		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();

		//OTS Edit
		wrapper.children().find("Memo()").at(2).dive().find("span").at(2).simulate("click");
		//OTS Delete
		wrapper.children().find("Memo()").at(2).dive().find("span").at(3).simulate("click");
		//MPA Edit
		wrapper.children().find("Memo()").at(2).dive().find("div.action").at(1).childAt(0).simulate("click");
		//MPA Delete
		wrapper.children().find("Memo()").at(2).dive().find("div.action").at(1).childAt(1).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it('render MachineList(OTS is exist, MPA is not exist)', () => {
		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={exceptMpaStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(2).dive().debug());
		//OTS Delete
		wrapper.children().find("Memo()").at(2).dive().find("span").at(3).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it('render MachineList(password is null)', () => {
		const wrapper = shallow(
			<Diagram
				dispatch={dispatch}
				store={passwordNotSetStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper.children", wrapper.children().find("Memo()").at(2).dive().debug());
		wrapper.children().find("Memo()").at(2).dive();
		expect(wrapper).toMatchSnapshot();
	});
});