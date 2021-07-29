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
import { initialState as autoPlanInit } from "../../../../modules/autoPlan";
import services from "../../../../services";

const mockStore = configureStore();
//const logSpy = jest.spyOn(console, 'log');
const dispatch = sinon.spy();
let store, newCommandInit, newProps, wrapper;

const initialStates = {
    query: "",
    showSearch: false,
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
        index: -1,
        id: -1,
        cmd_name: "none",
        cmd_type: "vftp_compat",
        checked: false
    },
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
        cmd_name: "IP_WS_BMP-%s-%s-SSS_TEST_1",
        cmd_type: "vftp_sss",
        checked: false
    },
    {
        index: 1,
        id: 3,
        cmd_name: "IP_AS_RAW-%s-%s-SSS_TEST_2",
        cmd_type: "vftp_sss",
        checked: false
    },
    {
        index:2,
        id:6,
        cmd_name:"IP_WS_BMP-%s-%s",
        cmd_type:"vftp_sss",
        checked: false
    },
    {
        index:3,
        id:12,
        cmd_name:"IP_AS_BMP-%s-%s",
        cmd_type:"vftp_sss",
        checked: false
    },
    {
        index:5,
        id:23,
        cmd_name:"IP_AS_RAW-%s-%s-SSS_TEST_5",
        cmd_type:"vftp_sss",
        checked: false
    }
];

const emptyLists = [];

services.axiosAPI.requestPost = jest.fn().mockResolvedValue({ status: Define.OK });
services.axiosAPI.requestPut = jest.fn().mockResolvedValue({ status: Define.OK });
services.axiosAPI.requestDelete = jest.fn().mockResolvedValue({ status: Define.OK });

describe('commandlist(Auto) common test', () => {
   it('renders with empty data', () => {
       const props = {
           command: commandInit,
           autoPlan: autoPlanInit
       };

       store = mockStore(props);
       wrapper = shallow(
           <CommandList
               type={Define.PLAN_TYPE_VFTP_COMPAT}
               states={initialStates}
               store={store}
               dispatch={dispatch}
           />);
       expect(wrapper).toMatchSnapshot();
   });

    it('renders with commandList.length is 0(COMMON)', () => {
        const emptyData = produce(commandInit.toJS(), draft => {
            draft.command.lists = emptyLists;
        });

        const emptyProps = {
            command: fromJS(emptyData),
            autoPlan: fromJS(produce(autoPlanInit.toJS(), draft => {
            }))
        };

        store = mockStore(emptyProps);
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_SSS}
                states={{ ...initialStates, isNewOpen: true }}
                store={store}
                dispatch={dispatch}
            />);

        wrapper.children().dive();
        wrapper.children().dive().find('Memo()').at(1).dive();
    });
});

describe('commandlist(Auto) VFTP/COMPAT test', () => {
    beforeEach(() => {
        newCommandInit = produce(commandInit.toJS(), draft => {
            draft.command.lists = compatLists;
        });

        newProps = {
            command: fromJS(newCommandInit),
            autoPlan: fromJS(produce(autoPlanInit.toJS(), draft => {}))
        };

        store = mockStore(newProps);
    });

    it('renders with data(VFTP/COMPAT)', async () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{ ...initialStates, isNewOpen: true }}
                store={store}
                dispatch={dispatch}
            />);

        //console.log("wrapper", wrapper.debug());
        //console.log("wrapper.children", wrapper.children().debug());
        //console.log("wrapper.children.dive", wrapper.children().dive().debug());

        wrapper.children().dive().find('Memo()').at(0).dive().find('ButtonToggle').at(0).simulate('click');
        //expect(logSpy).toBeCalledWith('handleSearchToggle called!');

        wrapper.children().dive().find('Memo()').at(0).dive().find('ButtonToggle').at(1).simulate('click');
        //expect(logSpy).toBeCalledWith('selectItem called!');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('ul')
            .childAt(0)
            .find('input').simulate('change', { target: { id: '0' }});
        //expect(logSpy).toBeCalledWith('handleCheckboxClick called!');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(0)
            .dive()
            .find('Input')
            .dive()
            .find('input')
            .simulate('change', { target: { value: 'TEST'}});
        //expect(logSpy).toBeCalledWith('handleSearch called!');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(0)
            .dive()
            .find('Button')
            .dive()
            .find('button')
            .simulate('click');
        //expect(logSpy).toBeCalledWith('openNewModal called!');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('Input')
            .at(0)
            .dive()
            .find('input')
            .simulate('change', { target: { value: "TEST"}});
        //expect(logSpy).toBeCalledWith('onDataTypeChange called!');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('Input')
            .at(1)
            .dive()
            .find('input')
            .simulate('change', { target: { value: "TEST"}});
        //expect(logSpy).toBeCalledWith('onContextChange called!');

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('addCommand called!');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(1)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('closeNewModal called!');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('ul')
            .childAt(1)
            .find('.icon')
            .at(0)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('openDeleteModal called!');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('ul')
            .childAt(1)
            .find('.icon')
            .at(1)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('openEditModal called!');
    });

    it('renders with data(VFTP/COMPAT) addCommand test 1', async () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{...initialStates, isNewOpen: true, currentContext: "TEST" }}
                store={store}
                dispatch={dispatch}
            />);

        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('addCommand called!');
    });

    it('renders with data(VFTP/COMPAT) saveCommand test 1', async () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{...initialStates, isEditOpen: true, currentContext: "TEST", actionId: 0 }}
                store={store}
                dispatch={dispatch}
            />);

        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('saveCommand called!');
    });

    it('renders with data(VFTP/COMPAT) saveCommand test 2', async () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{...initialStates, isEditOpen: true, actionId: 0 }}
                store={store}
                dispatch={dispatch}
            />);

        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('saveCommand called!');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(1)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('closeEditModal called!');
    });

    it('renders with data(VFTP/COMPAT) deleteModal test 1', async () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{...initialStates, isDeleteOpen: true }}
                store={store}
                dispatch={dispatch}
            />);

        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('deleteCommand called!');

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(1)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('closeDeleteModal called!');
    });

    it('renders with data(VFTP/COMPAT) errorModal test 1', () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{...initialStates, isErrorOpen: true }}
                store={store}
                dispatch={dispatch}
            />);

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        //expect(logSpy).toBeCalledWith('closeErrorModal called!');
    });

    it('renders with data(VFTP/COMPAT) errorModal test 2', () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{...initialStates, isErrorOpen: true, openedModal: 1 }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
        //expect(logSpy).toBeCalledWith('closeErrorModal called!');
    });

    it('renders with data(VFTP/COMPAT) errorModal test 3', () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{...initialStates, isErrorOpen: true, openedModal: 2 }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
        //expect(logSpy).toBeCalledWith('closeErrorModal called!');
    });

    it('renders with data(VFTP/COMPAT) render closedModal test', () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={initialStates}
                store={store}
                dispatch={dispatch}
            />);
        wrapper.children().dive().find('Memo()').at(2).dive();
    });

    it('renders with data(VFTP/COMPAT) render query search test', () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{ ...initialStates, query: "xxxx" }}
                store={store}
                dispatch={dispatch}
            />);

        wrapper.children().dive().find('Memo()').at(1).dive();
    });

    it('renders with data(VFTP/COMPAT) addModal error test 1', async () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{ ...initialStates, isNewOpen: true, currentContext: "none" }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/COMPAT) editModal error test 1', async () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{ ...initialStates, isEditOpen: true }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/COMPAT) editModal error test 2', async () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{ ...initialStates, isEditOpen: true, currentContext: "none" }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        await wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/COMPAT) editModal error test 3', () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{ ...initialStates, isEditOpen: true, openedModal: 1 }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });

    it('renders with data(VFTP/COMPAT) editModal error test 4', () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_COMPAT}
                states={{ ...initialStates, isEditOpen: true, openedModal: 2 }}
                store={store}
                dispatch={dispatch}
            />);

        jest.useFakeTimers();
        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(2)
            .dive()
            .find('.button-wrap')
            .childAt(0)
            .simulate('click');
        jest.advanceTimersByTime(1000);
        jest.useRealTimers();
    });
});

describe('commandlist(Auto) VFTP/SSS test', () => {
    beforeEach(() => {
        newCommandInit = produce(commandInit.toJS(), draft => {
            draft.command.lists = sssLists;
            draft.command.checkedCnt = sssLists.length;
        });

        newProps = {
            command: fromJS(newCommandInit),
            autoPlan: fromJS(produce(autoPlanInit.toJS(), draft => {}))
        };

        store = mockStore(newProps);
    });

    it('renders with data(VFTP/SSS)', () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_SSS}
                states={{ ...initialStates, isNewOpen: true }}
                store={store}
                dispatch={dispatch}
            />);

        wrapper.children().dive().find('Memo()').at(2).dive();
    });

    it('renders with data(VFTP/SSS) editModal test 1', () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_SSS}
                states={{ ...initialStates, isEditOpen: true }}
                store={store}
                dispatch={dispatch}
            />);

        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('ul')
            .childAt(1)
            .find('.icon')
            .at(1)
            .simulate('click');
        wrapper
            .children()
            .dive()
            .find('Memo()')
            .at(1)
            .dive()
            .find('ul')
            .childAt(2)
            .find('.icon')
            .at(1)
            .simulate('click');

        wrapper.children().dive().find('Memo()').at(2).dive();
    });

    it('renders with data(VFTP/SSS) showSearch test 1', () => {
        wrapper = shallow(
            <CommandList
                type={Define.PLAN_TYPE_VFTP_SSS}
                states={{ ...initialStates, showSearch: true }}
                store={store}
                dispatch={dispatch}
            />);

        wrapper.children().dive().find('Memo()').at(0).dive();
    });
});