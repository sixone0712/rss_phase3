import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import {fromJS, List, Map} from "immutable";
import sinon from "sinon";
import * as Define from "../../../../define";
import services from '../../../../services';
import CategoryList from "../CategoryList";
import {initialState as viewListInit} from "../../../../modules/viewList";
import produce from "immer";

const mockStore = configureStore();
const dispatch = sinon.spy();

const fullLogList = List([
	Map(
		{
			keyIndex: 0,
			logCode: "001",
			logName: "test code 001",
			description: "test purpose",
			dest:"local",
			filePath: "/jest/test",
			fileName: "result.html",
			auto: false,
			display: false,
			checked: false
		}
	),
	Map(
		{
			keyIndex: 1,
			logCode: "002",
			logName: "test code 002",
			description: "test purpose",
			dest:"local",
			filePath: "/jest/test",
			fileName: "result.html",
			auto: true,
			display: true,
			checked: false
		}
	),
]);

const nullLogList = List([]);

const initialState = {
	categoryModalOpen: false,
	alertOpen: false,
	alertMsg: "",
	onSearch: false,
	query: "",
	selectedIndex: 0
};

const alertState = {
	categoryModalOpen: false,
	alertOpen: false,
	alertMsg: "",
	onSearch: false,
	query: "",
	selectedIndex: 0
};

services.axiosAPI.requestGet = jest.fn().mockResolvedValue({ status: Define.OK });
services.axiosAPI.requestPost = jest.fn().mockResolvedValue({ status: Define.OK });
services.axiosAPI.requestPatch = jest.fn().mockResolvedValue({ status: Define.OK });

describe('CategoryList', () => {
	const defaultStore = mockStore({ viewList: fromJS(produce(viewListInit.toJS(), draft => { draft.logInfoList = nullLogList; })) });
	const fullStore = mockStore({ viewList: fromJS(produce(viewListInit.toJS(), draft => { draft.logInfoList = fullLogList; })) });

	it('renders correctly', () => {
		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={defaultStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		wrapper.children().find("Memo()").at(2).dive();
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(category is exist)', () => {
		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={{...initialState, onSearch: true}}
				openDelete={jest.fn()}/>).dive().dive();
		//console.log("wrapper: ", wrapper.children().find("button").debug());

		//toggle control
		wrapper.children().find("input").props().onChange(false);

		//change query
		wrapper.children().find("Input").dive().simulate("change", { target: { value: "test"}});

		//open add modal
		wrapper.children().find("button").simulate("click");

		wrapper.children().find("Memo()").at(2).dive();
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(query length > 0)', () => {
		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={{...initialState, query: "test"}}
				openDelete={jest.fn()}/>).dive().dive();
		wrapper.children().find("Memo()").at(2).dive();

		//open edit modal
		wrapper.children().find("Memo()").at(2).dive().find("span.action").at(0).simulate("click");

		//open delete modal
		wrapper.children().find("Memo()").at(2).dive().find("span.action").at(1).simulate("click");

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(query length === 0)', () => {
		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		wrapper.children().find("Memo()").at(2).dive();
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(category not found)', () => {
		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={{...initialState, query: "abc"}}
				openDelete={jest.fn()}/>).dive().dive();
		wrapper.children().find("Memo()").at(2).dive();
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(open add modal)', async () => {
		services.axiosAPI.requestGet =
			jest.fn().mockResolvedValueOnce(
				{
					status: Define.OK,
					data: {
						lists: [
							{
								detailedStatus: "completed",
								categoryCodes: "1"
							}
						]
					}
				});

		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();

		await wrapper.children().find("button").simulate("click");

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(open add modal(error))', async () => {
		services.axiosAPI.requestGet =
			jest.fn().mockResolvedValueOnce(
				{
					status: Define.OK,
					data: {
						lists: [
							{
								detailedStatus: "collecting",
								categoryCodes: "001"
							}
						]
					}
				});

		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();

		await wrapper.children().find("button").simulate("click");

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(close category modal)', async () => {
		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();

		await wrapper.children().find("Memo()").at(0).props().close();

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(open delete modal(no error))', async () => {
		services.axiosAPI.requestGet =
			jest.fn().mockResolvedValueOnce(
				{
					status: Define.OK,
					data: {
						lists: [
							{
								detailedStatus: "completed",
								categoryCodes: "5"
							}
						]
					}
				});

		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();

		await wrapper.children().find("Memo()").at(2).dive().find("span.action").at(1).simulate("click");

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(open delete modal(error))', async () => {
		services.axiosAPI.requestGet =
			jest.fn().mockResolvedValueOnce(
				{
					status: Define.OK,
					data: {
						lists: [
							{
								detailedStatus: "collecting",
								categoryCodes: "001"
							}
						]
					}
				});

		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();

		await wrapper.children().find("Memo()").at(2).dive().find("span.action").at(1).simulate("click");

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(add new category(no error))', async () => {
		services.axiosAPI.requestGet =
			jest.fn().mockResolvedValueOnce(
				{
					status: Define.OK,
					data: { lists: [ { categoryCode: "001" }]
					}
				});

		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();

		await wrapper.children().find("Memo()").at(0).props().apply(
			{
				logName :'test add',
				logNo : '3',
				logCode : '003',
				filePath : 'test/test',
				description : 'test',
				fileName : 'test.txt',
				display: false,
				auto: false,
				dest:'Cons'
			}, 0);

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(add new category(error))', async () => {
		services.axiosAPI.requestGet =
			jest.fn().mockResolvedValueOnce(
				{
					status: Define.OK,
					data: { lists: [ { categoryCode: "003" }]
					}
				});

		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();

		jest.useFakeTimers();
		await wrapper.children().find("Memo()").at(0).props().apply(
			{
				logName :'test add',
				logNo : '3',
				logCode : '003',
				filePath : 'test/test',
				description : 'test',
				fileName : 'test.txt',
				display: false,
				auto: false,
				dest:'Cons'
			}, 0);
		jest.advanceTimersByTime(1000);
		jest.useRealTimers();

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(edit category(no error))', async () => {
		services.axiosAPI.requestGet =
			jest.fn().mockResolvedValueOnce(
				{
					status: Define.OK,
					data: {
						lists: [
							{
								detailedStatus: "completed",
								categoryCodes: "001"
							}
						]
					}
				});

		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();

		await wrapper.children().find("Memo()").at(0).props().apply(
			{
				logName :'test add',
				logNo : '1',
				logCode : '001',
				filePath : 'test/test',
				description : 'test',
				fileName : 'test.txt',
				display: false,
				auto: false,
				dest:'Cons'
			}, 1);

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(edit category(error))', async () => {
		services.axiosAPI.requestGet =
			jest.fn().mockResolvedValueOnce(
				{
					status: Define.OK,
					data: {
						lists: [
							{
								detailedStatus: "collecting",
								categoryCodes: "001"
							}
						]
					}
				});

		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();

		await wrapper.children().find("Memo()").at(0).props().apply(
			{
				logName :'test add',
				logNo : '1',
				logCode : '001',
				filePath : 'test/test',
				description : 'test',
				fileName : 'test.txt',
				display: false,
				auto: false,
				dest:'Cons'
			}, 1);

		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(close alert modal(case 1))',  () => {
		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={initialState}
				openDelete={jest.fn()}/>).dive().dive();
		wrapper.children().find("Memo()").at(1).props().closer();
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(close alert modal(case 2))',  () => {
		const wrapper = shallow(
			<CategoryList
				dispatch={dispatch}
				store={fullStore}
				initialState={{...initialState, alertMsg: "The No. is already registered. Please register again."}}
				openDelete={jest.fn()}/>).dive().dive();

		jest.useFakeTimers();
		wrapper.children().find("Memo()").at(1).props().closer();
		jest.advanceTimersByTime(1000);
		jest.useRealTimers();

		expect(wrapper).toMatchSnapshot();
	});
});