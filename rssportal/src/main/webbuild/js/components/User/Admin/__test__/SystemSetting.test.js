import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import sinon from "sinon";
import * as Define from "../../../../define";
import services from '../../../../services';
import SystemSetting from "../SystemSetting";
import {initialState as viewListInit} from "../../../../modules/viewList";

const initialState = {
	importOpen: false,
	deleteOpen: false,
	alertOpen: false,
	modalMsg: null,
	modalIcon: null,
	totalUserCnt: 0,
	dlHistoryCnt: 0,
	sValue: 0
};

const mockStore = configureStore();
const dispatch = sinon.spy();

services.axiosAPI.requestGet = jest.fn().mockResolvedValue({ status: Define.OK, data: { HistoryCnt: 1, UserCnt: 1 } });
services.axiosAPI.requestDelete = jest.fn().mockResolvedValue({ status: Define.OK });

describe('SystemSetting', () => {
	const props = {	viewList: viewListInit };
	const store = mockStore(props);

	it('renders correctly(no error)', () => {
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		expect(wrapper).toMatchSnapshot();
	});

	it('renders correctly(error)', () => {
		services.axiosAPI.requestGet = jest.fn().mockRejectedValue(new Error("test error"));
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		expect(wrapper).toMatchSnapshot();
	});

	it("open import modal (plan is exist)", async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue({ status: Define.OK, data: { lists: [{ detailedStatus: "collecting" }] } });
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		await wrapper.children().find('button').at(0).simulate('click');
		expect(wrapper).toMatchSnapshot();
	});

	it("open import modal (plan is not exist)", async () => {
		services.axiosAPI.requestGet = jest.fn().mockResolvedValue({ status: Define.OK, data: { lists: [{ detailedStatus: "completed" }] } });
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		await wrapper.children().find('button').at(0).simulate('click');
		expect(wrapper).toMatchSnapshot();
	});

	it("close import modal", () => {
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(0).props().close();
		expect(wrapper).toMatchSnapshot();
	});

	it("open delete modal(Diagram)", () => {
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(1).props().openDelete("DIAGRAM", 0);
		expect(wrapper).toMatchSnapshot();
	});

	it("open delete modal(Category)", () => {
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(2).props().openDelete("CATEGORY", 0);
		expect(wrapper).toMatchSnapshot();
	});

	it("close delete modal(diagram) - Background", async () => {
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(1).props().openDelete("DIAGRAM", 0);
		await wrapper.children().find("Memo()").at(0).dive().find("div.Custom-modal-overlay").simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it("close delete modal(diagram) - Left button(no error)", async () => {
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(1).props().openDelete("DIAGRAM", 0);
		await wrapper.children().find("Memo()").at(0).dive().find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it("close delete modal(diagram) - Left button(error)", async () => {
		services.axiosAPI.requestDelete = jest.fn().mockRejectedValue(new Error("test error"));
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(1).props().openDelete("DIAGRAM", 0);
		await wrapper.children().find("Memo()").at(0).dive().find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it("close delete modal(diagram) - Right button", async () => {
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(1).props().openDelete("DIAGRAM", 0);
		await wrapper.children().find("Memo()").at(0).dive().find("button").at(1).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it("close delete modal(category) - Background", async () => {
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(2).props().openDelete("CATEGORY", 0);
		await wrapper.children().find("Memo()").at(0).dive().find("div.Custom-modal-overlay").simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it("close delete modal(category) - Left button(no error)", async () => {
		services.axiosAPI.requestDelete = jest.fn().mockResolvedValue({ status: Define.OK });
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(2).props().openDelete("CATEGORY", 0);
		await wrapper.children().find("Memo()").at(0).dive().find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it("close delete modal(category) - Left button(error)", async () => {
		services.axiosAPI.requestDelete = jest.fn().mockRejectedValue(new Error("test error"));
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(2).props().openDelete("CATEGORY", 0);
		await wrapper.children().find("Memo()").at(0).dive().find("button").at(0).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it("close delete modal(category) - Right button", async () => {
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find("Connect(Component)").at(2).props().openDelete("CATEGORY", 0);
		await wrapper.children().find("Memo()").at(0).dive().find("button").at(1).simulate("click");
		expect(wrapper).toMatchSnapshot();
	});

	it("close alert modal", async () => {
		const wrapper = shallow(
			<SystemSetting
				dispatch={dispatch}
				store={store}
				initialState={{...initialState, alertOpen: true}} />
				).dive().dive();
		wrapper.children().find("Memo()").at(1).dive().find("button").simulate("click");
		//console.log("wrapper.children: ", wrapper.children().find("Memo()").at(1).dive().debug());
		expect(wrapper).toMatchSnapshot();
	});

	it("open service manager", () => {
		const wrapper = shallow(<SystemSetting dispatch={dispatch} store={store} initialState={initialState} />).dive().dive();
		wrapper.children().find('button').at(1).simulate('click');
		expect(wrapper).toMatchSnapshot();
	});
});