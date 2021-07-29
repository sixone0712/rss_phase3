import 'babel-polyfill';
import React from 'react';
import renderer from 'react-test-renderer'
import configureMockStore from 'redux-mock-store'
import configureStore from 'redux-mock-store'
import { shallow, mount } from 'enzyme';
import {createStore} from 'redux';
import {Provider} from 'react-redux';
import { Map, List, fromJS, Record } from 'immutable';
import PlanList from "../PlanList";
import { statusType, detailType, CreateStatus, CreateDetail } from "../PlanList";
import sinon from "sinon";
import moment from "moment";
import * as Define from "../../../define";

import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import services from '../../../services';

services.axiosAPI.requestPut = jest.fn().mockResolvedValue();
services.axiosAPI.requestDelete = jest.fn().mockResolvedValue();

const initialState = {
    viewList: {
        get: (id) => {
            switch (id) {
                case "toolInfoList":
                    return (
                        List([
                                 Map({
                                keyIndex: 0,
                                structId: "CR7",
                                collectServerId: 0,
                                collectHostName: null,
                                targetname: "EQVM88",
                                targettype: "1",
                                checked: true
                            })
                     ]));
                case "logInfoList":
                    return (
                        List([
                            Map({
                                keyIndex: 1,
                                logType: 0,
                                logCode: "001",
                                logName: "001 RUNNING STATUS",
                                fileListForwarding: null,
                                checked: true
                            })
                    ]));

                default: return jest.fn();
            }
        }
    },
    autoPlan: {
        get: () => {
            return Map({
                planId: "test1",
                collectStart: moment().startOf('day'),
                from: moment().startOf('day'),
                to : moment().endOf('day'),
                collectType: Define.AUTO_MODE_CONTINUOUS,
                interval: 1,
                intervalUnit: Define.AUTO_UNIT_MINUTE,
                description: "this is test1"
            })
        }
    },
};

const mockStore = configureStore();
const dispatch = sinon.spy();
let store;
let props;
let localState;

describe('PlanList', () => {
    beforeEach(() => {
        services.axiosAPI.requestGet = jest.fn().mockResolvedValue({
            data: {
                lists: [
                    {
                        planId: 235,
                        planType: "ftp",
                        ownerId: "10001",
                        planName: "test1",
                        fabNames: [
                            "Fab1", "Fab2"
                        ],
                        machineNames: [
                            "MPA_1", "MPA_2"
                        ],
                        categoryCodes: [
                            "002", "003"
                        ],
                        categoryNames: [
                            "002_TEST_2_LOG", "003_TEST_3_LOG"
                        ],
                        commands: [],
                        type: "continuous",
                        interval: "0",
                        description: "test1234",
                        start: "20200827000000",
                        from: "20200827000000",
                        to: "20200827235959",
                        lastCollection: "20200827142357",
                        status: "running",
                        detailStatus: "registered"
                    },
                    {
                        planId: 49,
                        planType: "vftp_sss",
                        ownerId: "10018",
                        planName: "vftp_sss_test",
                        fabNames: [
                            "Fab1", "Fab2"
                        ],
                        machineNames: [
                            "MPA_1", "MPA_2"
                        ],
                        categoryCodes: [],
                        categoryNames: [],
                        commands: [
                            "IP_AS_RAW-%s-%s",
                            "IP_AS_RAW-%s-%s-DE_TEST_11111"
                        ],
                        type: "cycle",
                        interval: "600000",
                        description: "vftp sss test",
                        start: "20200827000000",
                        from: "20200827000000",
                        to: "20200827235959",
                        lastCollection: null,
                        status: "stopped",
                        detailStatus: "completed"
                    },
                    {
                        planId: 43,
                        planType: "vftp_compat",
                        ownerId: "10005",
                        planName: "vftp_compat_test",
                        fabNames: [
                            "Fab1", "Fab2"
                        ],
                        machineNames: [
                            "MPA_1", "MPA_2"
                        ],
                        categoryCodes: [],
                        categoryNames: [],
                        commands: [
                            "none",
                            "%s-%s-DE_TEST_COMPAT"
                        ],
                        type: "cycle",
                        interval: "3600000",
                        description: "vftp compat test",
                        start: "20200827000000",
                        from: "20200827000000",
                        to: "20200827235959",
                        lastCollection: null,
                        status: "running",
                        detailStatus: "collecting"
                    },
                    {
                        planId: 999,
                        planType: "invalid type",
                        ownerId: "10020",
                        planName: "invalid test",
                        fabNames: [
                            "Fab1", "Fab2"
                        ],
                        machineNames: [
                            "MPA_1", "MPA_2"
                        ],
                        categoryCodes: [],
                        categoryNames: [],
                        commands: [],
                        type: "cycle",
                        interval: "3600000",
                        description: "invalid test",
                        start: "20200827000000",
                        from: "20200827000000",
                        to: "20200827235959",
                        lastCollection: null,
                        status: "running",
                        detailStatus: "collecting"
                    }
                ]
            }
        });
        props = {
            history: {
                push: jest.fn()
            }
        }
        localState = {
            registeredList : [{
                planId: "test1",
                planDescription: "test1234",
                planTarget: 2,
                planPeriodStart: "2020-08-27 00:00:00",
                planPeriodEnd: "2020-08-27 23:59:59",
                planStatus: "running",
                planLastRun: "-",
                planDetail: "registered",
                id: 235,
                tool: [
                    "MPA_1", "MPA_2"
                ],
                logType: [
                    "002", "003"
                ],
                interval: "0",
                collectStart: "2020-08-27 00:00:00",
                collectTypeStr: "continuous",
                keyIndex: 1,
                planType: "ftp",
                commandCount: 0,
                commands: []
            }]
        };
    });

    it('renders correctly(data exist', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        expect(wrapper).toMatchSnapshot();
    });

    it('renders correctly(data does not exit', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        expect(wrapper).toMatchSnapshot();
    });

    it('setEditPlanList', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        wrapper.instance().setEditPlanList(235, statusType.RUNNING);
        wrapper.instance().setEditPlanList(235, statusType.STOPPED);
    });

    it('openDeleteModal', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        wrapper.instance().openDeleteModal (235, statusType.RUNNING);
        wrapper.instance().openDeleteModal (235, statusType.STOPPED);
    });

    it('closeDeleteModal', async () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);

        await wrapper.instance().closeDeleteModal(false, 235);

        services.axiosAPI.requestGet = jest.fn().mockResolvedValue({status: 200});
        await wrapper.instance().closeDeleteModal(true, 235);

        services.axiosAPI.requestGet = jest.fn().mockResolvedValue({status: 0});
        await wrapper.instance().closeDeleteModal(true, 235);

        wrapper.setState({
            ...localState,
            deleteIndex: 1
        })
        await wrapper.instance().closeDeleteModal(false, 235);
    });

    it('openStatusModal', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        wrapper.instance().openStatusModal(statusType.RUNNING, 235);
        wrapper.instance().openStatusModal(statusType.STOPPED, 235);
    });

    it('closeStatusModal', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        wrapper.instance().closeStatusModal();
    });

    it('openAlert, closeAlert', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        wrapper.instance().openAlert();
        wrapper.instance().closeAlert();
    });

    it('handlePaginationChange', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        wrapper.instance().handlePaginationChange(1);
    });

    it('handleSelectBoxChange', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        wrapper.instance().handleSelectBoxChange(1);

        wrapper.setState({
            currentPage: 2
        });
        wrapper.instance().handleSelectBoxChange(1);
    });

    it('stopDownload', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        services.axiosAPI.requestGet = jest.fn().mockResolvedValue();
        wrapper.instance().stopDownload(245);
    });

    it('restartDownload', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        services.axiosAPI.requestGet = jest.fn().mockResolvedValue();
        wrapper.instance().restartDownload(245);
    });

    it('handleStatusChange', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanList
            dispatch={dispatch}
            store={store}
            {...props}
        />).dive().dive();
        wrapper.setState(localState);
        wrapper.instance().handleStatusChange(statusType.RUNNING, 245);
        wrapper.instance().handleStatusChange(statusType.STOPPED, 245);
        wrapper.instance().handleStatusChange(-1, 245);
    });
});

describe('CreateStatus', () => {
    it('call CreateStatus', () => {
        CreateStatus(statusType.RUNNING, jest.fn());
        CreateStatus(statusType.STOPPED, jest.fn());
        CreateStatus(-1, jest.fn());
    });
});

describe('CreateDetail', () => {
    it('call CreateDetail', () => {
        CreateDetail(detailType.REGISTERED);
        CreateDetail(detailType.COLLECTING);
        CreateDetail(detailType.COLLECTED);
        CreateDetail(detailType.SUSPENDED);
        CreateDetail(detailType.HALTED);
        CreateDetail(detailType.COMPLETED);
        CreateDetail(-1);
    });
});


