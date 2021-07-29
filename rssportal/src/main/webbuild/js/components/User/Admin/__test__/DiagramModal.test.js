import 'babel-polyfill';
import React, { useRef } from 'react';
import { shallow } from 'enzyme';
import DiagramModal from "../DiagramModal";

jest.mock('react', () => {
	const originReact = jest.requireActual('react');
	const mUseRef = jest.fn();
	return {
		...originReact,
		useRef: mUseRef,
	};
});

const mErrRef = {
	current:
		{
			children: [
				{ children: [{	id: "", value: "" }, {	id: "", value: "" }] },
				{ children: [{	id: "mpa_name", value: "mpa_name" }, {	id: "mpa_name", value: "" }] },
				{ children: [{	id: "mpa_ip", value: "mpa_ip" }, {	id: "mpa_ip", value: "mpa_ip" }] },
				{ children: [{	id: "mpa_line", value: "mpa_line" }, {	id: "mpa_line", value: "mpa_line" }] },
				{ children: [{	id: "mpa_ttype", value: "mpa_ttype" }, {	id: "mpa_ttype", value: "mpa_ttype" }] },
				{ children: [{	id: "mpa_serial", value: "mpa_serial" }, {	id: "mpa_serial", value: "mpa_serial" }] },
				{ children: [{	id: "mpa_ftpuser", value: "mpa_ftpuser" }, {	id: "mpa_ftpuser", value: "mpa_ftpuser" }] },
				{ children: [{	id: "mpa_ftppw", value: "mpa_ftppw" }, {	id: "mpa_ftppw", value: "mpa_ftppw" }] },
				{ children: [{	id: "mpa_vftpuser", value: "mpa_vftpuser" }, {	id: "mpa_vftpuser", value: "mpa_vftpuser" }] },
				{ children: [{	id: "mpa_vftppw", value: "mpa_vftppw" }, {	id: "mpa_vftppw", value: "mpa_vftppw" }] }
			]
		}
};

const mRef = {
	current:
		{
			children: [
				{ children: [{	id: "", value: "" }, {	id: "", value: "" }] },
				{ children: [{	id: "mpa_name", value: "mpa_name" }, {	id: "mpa_name", value: "mpa_name" }] },
				{ children: [{	id: "mpa_ip", value: "mpa_ip" }, {	id: "mpa_ip", value: "mpa_ip" }] },
				{ children: [{	id: "mpa_line", value: "mpa_line" }, {	id: "mpa_line", value: "mpa_line" }] },
				{ children: [{	id: "mpa_ttype", value: "mpa_ttype" }, {	id: "mpa_ttype", value: "mpa_ttype" }] },
				{ children: [{	id: "mpa_serial", value: "mpa_serial" }, {	id: "mpa_serial", value: "mpa_serial" }] },
				{ children: [{	id: "mpa_ftpuser", value: "mpa_ftpuser" }, {	id: "mpa_ftpuser", value: "mpa_ftpuser" }] },
				{ children: [{	id: "mpa_ftppw", value: "mpa_ftppw" }, {	id: "mpa_ftppw", value: "mpa_ftppw" }] },
				{ children: [{	id: "mpa_vftpuser", value: "mpa_vftpuser" }, {	id: "mpa_vftpuser", value: "mpa_vftpuser" }] },
				{ children: [{	id: "mpa_vftppw", value: "mpa_vftppw" }, {	id: "mpa_vftppw", value: "mpa_vftppw" }] }
			]
		}
};

const oErrRef = {
	current:
		{
			children: [
				{ children: [{	id: "ots_name", value: "" }, {	id: "ots_name", value: "" }] },
				{ children: [{	id: "ots_ip", value: "" }, {	id: "ots_ip", value: "" }] },
				{ children: [{	id: "ots_port", value: "" }, {	id: "ots_port", value: "" }] },
			]
		}
};

const initialState = {
	selectMachine: "",
	mpaInputError: [],
	otsInputError: [],
	initLoader: false,
	mpaInfo: {
		host: '',
		line: '',
		ots: '',
		ftpPassword: '',
		ftpUser: '',
		vftpPassword: '',
		vftpUser: '',
		serialNumber: '',
		structId: '',
		targetname: '',
		toolType: ''
	},
	otsInfo: {
		host: '',
		targetname: '',
		port:''
	}
};

const otsData = 	{
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
};

const mpaData = {
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
};

const otsList = [
	{
		targetname: "OTS 01",
		host: "xxx.xxx.xxx.xxx",
		port: 80
	},
	{
		targetname: "OTS 02",
		host: "xxx.xxx.xxx.xxx",
		port: 80
	},
	{
		targetname: "OTS 03",
		host: "xxx.xxx.xxx.xxx",
		port: 80
	}
]

describe('DiagramModal', () => {
	it('renders correctly(closed modal)', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={false}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				otsList={null}
				eMachine={0}
				initialState={initialState}
			/>);
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(open add modal)', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				otsList={otsList}
				eMachine={0}
				initialState={initialState}
			/>);
		expect(wrapper).toMatchSnapshot();
	});

	it('change tab', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				otsList={otsList}
				eMachine={0}
				initialState={initialState}
			/>);
		//console.log("wrapper: ", wrapper.find("[type='radio']").at(1).debug());
		wrapper.find("[type='radio']").at(1).simulate("click");
		wrapper.find("[type='radio']").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it('change ots', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				otsList={otsList}
				eMachine={0}
				initialState={{...initialState, mpaInputError: ["mpa_ots", "mpa_name"] }}
			/>);
		wrapper.find("Select").props().onChange("OTS 02");
		expect(wrapper).toMatchSnapshot();
	});

	it('mpa change ots(error)', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				otsList={otsList}
				eMachine={0}
				initialState={initialState}
			/>);
		wrapper.find("Select").props().onChange("");
		expect(wrapper).toMatchSnapshot();
	});

	it('mpa change text box value', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				otsList={otsList}
				eMachine={0}
				initialState={
					{
						...initialState,
						selectMachine: "MPA",
						mpaInputError: [
							"mpa_ots",
							"mpa_name",
							"mpa_ip",
							"mpa_line",
							"mpa_ttype",
							"mpa_serial",
							"mpa_ftpuser",
							"mpa_ftppw",
							"mpa_vftpuser",
							"mpa_vftppw"
						]
					}
				}
			/>);
		//console.log("wrapper: ", wrapper.find("Input").at(0).dive().props());
		wrapper.find("Input").at(0).dive().props().onChange(
			{
				target:	{
					id: "mpa_name",
					name: "targetname",
					value: "test machine",
					maxLength: "50"
				}
			});

		wrapper.find("Input").at(0).dive().props().onChange(
			{
				target:	{
					id: "mpa_name",
					name: "targetname",
					value: "",
					maxLength: "50"
				}
			});

		wrapper.find("Input").at(1).dive().props().onChange(
			{
				target:	{
					id: "mpa_ip",
					name: "host",
					value: "xxx.xxx.xxx.xxx",
					maxLength: "50"
				}
			});

		wrapper.find("Input").at(2).dive().props().onChange(
			{
				target:	{
					id: "mpa_line",
					name: "line",
					value: "test line",
					maxLength: "25"
				}
			});

		wrapper.find("Input").at(3).dive().props().onChange(
			{
				target:	{
					id: "mpa_ttype",
					name: "toolType",
					value: "test tool type",
					maxLength: "25"
				}
			});

		wrapper.find("Input").at(4).dive().props().onChange(
			{
				target:	{
					id: "mpa_serial",
					name: "serialNumber",
					value: "test serial number",
					maxLength: "25"
				}
			});

		wrapper.find("Input").at(5).dive().props().onChange(
			{
				target:	{
					id: "mpa_ftpuser",
					name: "ftpUser",
					value: "test ftp user",
					maxLength: "20"
				}
			});

		wrapper.find("Input").at(6).dive().props().onChange(
			{
				target:	{
					id: "mpa_ftppw",
					name: "ftpPassword",
					value: "test ftp password",
					maxLength: "20"
				}
			});

		wrapper.find("Input").at(7).dive().props().onChange(
			{
				target:	{
					id: "mpa_vftpuser",
					name: "vftpUser",
					value: "test vftp user",
					maxLength: "20"
				}
			});

		wrapper.find("Input").at(8).dive().props().onChange(
			{
				target:	{
					id: "mpa_vftppw",
					name: "vftpPassword",
					value: "test vftp password",
					maxLength: "20"
				}
			});

		expect(wrapper).toMatchSnapshot();
	});

	it('ots change text box value', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				otsList={otsList}
				eMachine={0}
				initialState={{...initialState, selectMachine: "OTS",	otsInputError: ["ots_name",	"ots_ip", "ots_port"]}}
			/>);

		wrapper.find("Input").at(9).dive().props().onChange(
			{
				target:	{
					id: "ots_name",
					name: "targetname",
					value: "test ots name",
					maxLength: "25"
				}
			});

		wrapper.find("Input").at(9).dive().props().onChange(
			{
				target:	{
					id: "ots_name",
					name: "targetname",
					value: "",
					maxLength: "25"
				}
			});

		wrapper.find("Input").at(10).dive().props().onChange(
			{
				target:	{
					id: "ots_ip",
					name: "host",
					value: "xxx.xxx.xxx.xxx",
					maxLength: "25"
				}
			});

		wrapper.find("Input").at(11).dive().props().onChange(
			{
				target:	{
					id: "ots_port",
					name: "port",
					value: "80",
					maxLength: "6"
				}
			});

		expect(wrapper).toMatchSnapshot();
	});

	it('close modal', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				otsList={otsList}
				eMachine={0}
				initialState={initialState}
			/>);
		wrapper.find("button").at(1).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(open edit modal(MPA) case 1)', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={mpaData}
				otsList={otsList}
				eMachine={1}
				initialState={initialState}
			/>);
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(open edit modal(MPA) case 2)', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={mpaData}
				otsList={otsList}
				eMachine={1}
				initialState={{...initialState, mpaInfo: mpaData}}
			/>);
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(open edit modal(OTS))', () => {
		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={otsData}
				otsList={otsList}
				eMachine={1}
				initialState={{...initialState, mpaInputError: ["mpa_name"]}}
			/>);
		expect(wrapper).toMatchSnapshot();
	});

	it('apply modal(mpa, error case)', () => {
		useRef.mockReturnValue(mErrRef);

		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				otsList={otsList}
				eMachine={0}
				initialState={{...initialState, selectMachine: "MPA"}}
			/>);
		wrapper.find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it('apply modal(mpa, normal case)', () => {
		useRef.mockReturnValue(mRef);

		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn().mockReturnValue(0)}
				close={jest.fn()}
				data={null}
				otsList={otsList}
				eMachine={0}
				initialState={{...initialState, selectMachine: "MPA"}}
			/>);
		wrapper.find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it('apply modal(ots, error case)', () => {
		useRef.mockReturnValue(oErrRef);

		const wrapper = shallow(
			<DiagramModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				otsList={otsList}
				eMachine={0}
				initialState={{...initialState, selectMachine: "OTS"}}
			/>);
		wrapper.find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});
});