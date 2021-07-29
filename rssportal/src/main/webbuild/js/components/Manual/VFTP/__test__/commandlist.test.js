import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import { fromJS } from 'immutable';
import sinon from "sinon";
import produce from 'immer';

import CommandList from "../commandlist";
import * as Define from "../../../../define";
import { initialState as commandInit } from "../../../../modules/command";
import services from "../../../../services";
import {withHooks} from "jest-react-hooks-shallow/lib/enable-hooks";

const mockStore = configureStore();
const dispatch = sinon.spy();
let store, newCommandInit, newProps;
const initialStates = {
    selectCommand: -1,
    actionId: "",
    currentDateType: "",
    currentContext: "",
    errorMsg: "",
    isNewOpen: false,
    isEditOpen: false,
    isDeleteOpen: false,
    isErrorOpen: false,
    openedModal: ""
}

const compatLists = [
    {
        index: 0,
        id: 1,
        cmd_name: "%s-%s-COMPAT_TEST_1",
        cmd_type: "vftp_compat",
        checked: false
    },
    {
        index: 1,
        id: 3,
        cmd_name: "%s-%s-COMPAT_TEST_2",
        cmd_type: "vftp_compat",
        checked: false
    },
    {
        index:2,
        id:6,
        cmd_name:"%s-%s-COMPAT_TEST_3",
        cmd_type:"vftp_compat",
        checked: false
    },
    {
        index:3,
        id:12,
        cmd_name:"%s-%s-COMPAT_TEST_4",
        cmd_type:"vftp_compat",
        checked: false
    },
    {
        index:5,
        id:23,
        cmd_name:"%s-%s-COMPAT_TEST_5",
        cmd_type:"vftp_compat",
        checked: false
    }
];

const sssLists = [
    {
        index: 0,
        id: 1,
        cmd_name: "IP_WS_BMP-SSS_TE-ST_1",
        cmd_type: "vftp_sss",
        checked: false
    },
    {
        index: 1,
        id: 3,
        cmd_name: "IP_AS_RAW-SSS_TEST_2",
        cmd_type: "vftp_sss",
        checked: false
    },
    {
        index:2,
        id:6,
        cmd_name:"IP_WS_BMP",
        cmd_type:"vftp_sss",
        checked: false
    },
    {
        index:3,
        id:12,
        cmd_name:"IP_AS_BMP",
        cmd_type:"vftp_sss",
        checked: false
    },
    {
        index:5,
        id:23,
        cmd_name:"IP_AS_RAW-SSS_TEST_5",
        cmd_type:"vftp_sss",
        checked: false
    }
];

const emptyLists = [];

describe('commandlist(Manual)', () => {
    it('renders with empty data(VFTP/COMPAT)', () => {
        const props = { command: commandInit };
        store = mockStore(props);
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={initialStates}
                store={store}
                dispatch={dispatch}
            />);
        expect(wrapper).toMatchSnapshot();
    });

    it('renders with commandList.length is 0(COMMON)', () => {
        newCommandInit = produce(commandInit.toJS(), draft => {
            draft.command.lists = emptyLists;
        });
        newProps = { command: fromJS(newCommandInit) };
        store = mockStore(newProps);
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_SSS}
                states={initialStates}
                store={store}
                dispatch={dispatch}
            />);

        wrapper.children().dive().find('Memo()').at(0).dive();
    });
});

describe('commandlist(Manual) VFTP/COMPAT', () => {
    beforeEach(() => {
        newCommandInit = produce(commandInit.toJS(), draft => {
            draft.command.lists = compatLists;
        });
        newProps = { command: fromJS(newCommandInit) };
        store = mockStore(newProps);
    });

    it('renders with data(VFTP/COMPAT) common test', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={initialStates}
                store={store}
                dispatch={dispatch}
            />);

        //console.log("wrapper", wrapper.debug());
        //console.log("wrapper.children", wrapper.children().debug());

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive();

        wrapper
            .children()
            .dive()
            .find('ul')
            .childAt(0)
            .find('CustomInput')
            .dive()
            .find('input')
            .simulate('change', { target: { id: '-1' }});

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(0)
            .dive()
            .find('li')
            .at(0)
            .find('CustomInput')
            .dive()
            .find('input')
            .simulate('change', { target: { id: '1' }});

        wrapper
            .children()
            .dive()
            .find('.card-btn-area')
            .children()
            .dive()
            .find('button')
            .simulate('click');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(0)
            .dive()
            .find('li')
            .at(0)
            .find('.icon')
            .at(1)
            .simulate('click');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(0)
            .dive()
            .find('li')
            .at(0)
            .find('.icon')
            .at(0)
            .simulate('click');
    });

    it('renders with data(VFTP/COMPAT) AddModal test case 1', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isNewOpen: true
                }}
                store={store}
                dispatch={dispatch}
            />);

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('Input')
            .at(1)
            .dive()
            .find('input')
            .simulate('change', { target: { value: "TEST"}});

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(1)
            .simulate('click');
    });

    it('renders with data(VFTP/COMPAT) AddModal test case 2', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isNewOpen: true,
                    currentContext: "none"
                }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/COMPAT) AddModal test case 3', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isNewOpen: true,
                    currentContext: "NEW_COMMAND"
                }}
                store={store}
                dispatch={dispatch}
            />);

        services.axiosAPI.requestPost = jest.fn().mockResolvedValue({
            data: { id: 100 },
            status: Define.OK
        });
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
    });

    it('renders with data(VFTP/COMPAT) editModal test case 1', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isEditOpen: true
                }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(1)
            .simulate('click');
    });

    it('renders with data(VFTP/COMPAT) editModal test case 2', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isEditOpen: true,
                    currentContext: "COMPAT_TEST_5"
                }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/COMPAT) editModal test case 3', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isEditOpen: true,
                    currentContext: "none"
                }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/COMPAT) editModal test case 4', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isEditOpen: true,
                    currentContext: "EDIT_COMMAND",
                    selectCommand: 1
                }}
                store={store}
                dispatch={dispatch}
            />);

        services.axiosAPI.requestPut = jest.fn().mockResolvedValue({ status: Define.OK });
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
    });

    it('renders with data(VFTP/COMPAT) deleteModal test case 1', () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isDeleteOpen: true
                }}
                store={store}
                dispatch={dispatch}
            />);

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(1)
            .simulate('click');
    });

    it('renders with data(VFTP/COMPAT) deleteModal test case 2', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isDeleteOpen: true,
                    actionId: 1,
                    selectCommand: 1
                }}
                store={store}
                dispatch={dispatch}
            />);

        services.axiosAPI.requestDelete = jest.fn().mockResolvedValue({ status: Define.OK });
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
    });

    it('renders with data(VFTP/COMPAT) deleteModal test case 3', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isDeleteOpen: true,
                    actionId: 1,
                    selectCommand: 2
                }}
                store={store}
                dispatch={dispatch}
            />);

        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
    });

    it('renders with data(VFTP/COMPAT) errorModal test case 1', () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isErrorOpen: true,
                    openedModal: 1
                }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/COMPAT) errorModal test case 2', () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{
                    ...initialStates,
                    isErrorOpen: true,
                    openedModal: 2
                }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });
});

describe('commandlist(Manual) VFTP/SSS', () => {
    beforeEach(() => {
        newCommandInit = produce(commandInit.toJS(), draft => {
            draft.command.lists = sssLists;
        });
        newProps = { command: fromJS(newCommandInit) };
        store = mockStore(newProps);
    });

    it('renders with data(VFTP/SSS)', () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_SSS}
                states={initialStates}
                store={store}
                dispatch={dispatch}
            />);

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(0)
            .dive()
            .find('li')
            .at(1)
            .find('.icon')
            .at(1)
            .simulate('click');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(0)
            .dive()
            .find('li')
            .at(2)
            .find('.icon')
            .at(1)
            .simulate('click');
    });

    it('renders with data(VFTP/SSS) addModal test case 1', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_SSS}
                states={{
                    ...initialStates,
                    isNewOpen: true,
                    currentDateType: "TEST"
                }}
                store={store}
                dispatch={dispatch}
            />);

        //wrapper.children().dive().find('Memo()').at(1).dive();
        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/SSS) editModal test case 2', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_SSS}
                states={{
                    ...initialStates,
                    isNewOpen: true,
                    currentDateType: "TEST",
                    currentContext: "TEST"
                }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/SSS) editModal test case 1', () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_SSS}
                states={{
                    ...initialStates,
                    isEditOpen: true
                }}
                store={store}
                dispatch={dispatch}
            />);

        wrapper.children().dive().find('Memo()').at(1).dive();
        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('Input')
            .at(0)
            .dive()
            .simulate('change', { target: { value: 'TEST'}});
    });

    it('renders with data(VFTP/SSS) editModal test case 2', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_SSS}
                states={{
                    ...initialStates,
                    isEditOpen: true,
                    currentDateType: "TEST"
                }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/SSS) editModal test case 3', async () => {
        const wrapper = shallow(
            <CommandList
                cmdType={Define.PLAN_TYPE_VFTP_SSS}
                states={{
                    ...initialStates,
                    isEditOpen: true,
                    currentDateType: "TEST",
                    currentContext: "TEST"
                }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });
});