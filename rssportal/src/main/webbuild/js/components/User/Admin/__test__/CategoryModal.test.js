import 'babel-polyfill';
import React, { useRef } from 'react';
import { shallow } from 'enzyme';
import CategoryModal from "../CategoryModal";

jest.mock('react', () => {
	const originReact = jest.requireActual('react');
	const mUseRef = jest.fn();
	return {
		...originReact,
		useRef: mUseRef,
	};
});

const initialState = {
	initLoader: false,
	inputError: [],
	CategoryInfo: {
		logName :'',
		logNo : '',
		logCode : '',
		filePath : '',
		description : '',
		fileName : '',
		display: null,
		auto: null,
		dest: 'Cons'
	}
};

const editData = [{
	logName : 'test log',
	logNo : '1',
	logCode : '001',
	filePath : '/test/test',
	description : 'test log',
	fileName : 'result.html',
	display: true,
	auto: true,
	dest: null
}];

describe('CategoryModal', () => {
	it('renders correctly(closed modal)', () => {
		const wrapper = shallow(
			<CategoryModal
				isOpen={false}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				isEdit={0}
				initialState={initialState}
			/>);
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(opened modal)', () => {
		const wrapper = shallow(
			<CategoryModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				isEdit={0}
				initialState={initialState}
			/>);
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(input error)', () => {
		const wrapper = shallow(
			<CategoryModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				isEdit={0}
				initialState={{...initialState, inputError: ["logNo", "logName", "filePath", "fileName", "description"]}}
			/>);
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(close modal)', () => {
		const wrapper = shallow(
			<CategoryModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				isEdit={0}
				initialState={initialState}
			/>);
		//console.log("wrapper: ", wrapper.children().debug());

		//click overlay
		wrapper.children().find("div.Custom-modal-overlay").simulate("click");

		//click right button
		wrapper.children().find("button").at(1).simulate("click");

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(change input)', () => {
		const wrapper = shallow(
			<CategoryModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				isEdit={0}
				initialState={{...initialState, inputError: ["filePath"]}}
			/>);

		//change No.
		wrapper.find("Input").at(0).dive().props().onChange(
			{
				target:	{
					name: "logNo",
					value: "1",
					maxLength: "3"
				}
			});

		//change No.
		wrapper.find("Input").at(0).dive().props().onChange(
			{
				target:	{
					name: "logNo",
					value: "1234567890",
					maxLength: "3"
				}
			});

		//change Category Name
		wrapper.find("Input").at(1).dive().props().onChange(
			{
				target:	{
					name: "logName",
					value: "",
					maxLength: "50"
				}
			});

		//change File Path
		wrapper.find("Input").at(2).dive().props().onChange(
			{
				target:	{
					name: "filePath",
					value: "/test/test",
					maxLength: "150"
				}
			});

		//change File Name
		wrapper.find("Input").at(3).dive().props().onChange(
			{
				target:	{
					name: "fileName",
					value: "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest.txt",
					maxLength: "50"
				}
			});

		//change Description
		wrapper.find("Input").at(4).dive().props().onChange(
			{
				target:	{
					name: "description",
					value: "",
					maxLength: "50"
				}
			});

		//change Display
		wrapper.find("input[type='checkbox']").at(0).simulate("change", { target: {name: "display", checked: true}});

		//change Auto Download
		wrapper.find("input[type='checkbox']").at(1).simulate("change", { target: {name: "auto", checked: false}});

		//change location
		wrapper.find("Select").props().onChange("Logsv");

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(opened edit modal)', () => {
		const wrapper = shallow(
			<CategoryModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={editData}
				isEdit={"001"}
				initialState={{...initialState, CategoryInfo: editData}}
			/>);

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(apply modal(error))', () => {
		const cRef = {
			current:
				{
					children: [
						{ children: [{ name: "logNo", value: "" }, { name: "logNo", value: "" }] },
						{ children: [{ name: "logName", value: "test log" }, { name: "logName", value: "test log" }] },
						{ children: [{ name: "filePath", value: "test/test" }, { name: "filePath", value: "test/test" }] },
						{ children: [{ name: "fileName", value: "test.txt" }, {	name: "fileName", value: "test.txt" }] }
					]
				}
		};

		useRef.mockReturnValueOnce(cRef);

		const wrapper = shallow(
			<CategoryModal
				isOpen={true}
				apply={jest.fn()}
				close={jest.fn()}
				data={null}
				isEdit={0}
				initialState={initialState}
			/>);

		wrapper.children().find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(apply modal)', () => {
		const cRef = {
			current:
				{
					children: [
						{ children: [{ name: "logNo", value: "" }, { name: "logNo", value: "005" }] },
						{ children: [{ name: "logName", value: "test log" }, { name: "logName", value: "test log" }] },
						{ children: [{ name: "filePath", value: "test/test" }, { name: "filePath", value: "test/test" }] },
						{ children: [{ name: "fileName", value: "test.txt" }, {	name: "fileName", value: "test.txt" }] }
					]
				}
		};

		useRef.mockReturnValueOnce(cRef);

		const wrapper = shallow(
			<CategoryModal
				isOpen={true}
				apply={jest.fn().mockReturnValueOnce(0)}
				close={jest.fn()}
				data={null}
				isEdit={0}
				initialState={initialState}
			/>);

		wrapper.children().find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});
});