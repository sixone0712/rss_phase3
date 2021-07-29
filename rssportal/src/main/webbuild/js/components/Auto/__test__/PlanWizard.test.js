import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import { Map, List } from 'immutable';
import PlanWizard from "../PlanWizard";
import { wizardStep, modalType, invalidCheck } from "../PlanWizard";
import { initialState as commandInit } from "../../../modules/command";
import sinon from "sinon";
import moment from "moment";
import * as Define from "../../../define";

import services from '../../../services';

const penderSuccess = {
    success: {
        'viewList/VIEW_LOAD_TOOLINFO_LIST': true,
        'viewList/VIEW_LOAD_LOGTYPE_LIST': true,
    },
    failure: {
        'viewList/VIEW_LOAD_TOOLINFO_LIST': false,
        'viewList/VIEW_LOAD_LOGTYPE_LIST': false,
    },
}
const penderFailure = {
    success: {
        'viewList/VIEW_LOAD_TOOLINFO_LIST': false,
        'viewList/VIEW_LOAD_LOGTYPE_LIST': false,
    },
    failure: {
        'viewList/VIEW_LOAD_TOOLINFO_LIST': true,
        'viewList/VIEW_LOAD_LOGTYPE_LIST': true,
    },
}

const initialState = {
    viewList: {
        get: (id) => {
            switch (id) {
                case "toolInfoListCheckCnt": return 1;
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
                case "logInfoListCheckCnt": return 1;
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
    command: {
        get: () => {
            return (
                List([
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
                        checked: true
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
                        checked: true
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
                ])
            );
        }
    },
    pender: penderSuccess
};

const mockStore = configureStore();
const dispatch = sinon.spy();
let store;

describe('PlanWiard', () => {

    /*
    beforeEach(() => {
        store = mockStore(initialState);
    });
    */

    it('renders correctly', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanWizard
            dispatch={dispatch}
            store={store}
            isNew = {true}
            editId = {0}
        />).dive().dive();
        expect(wrapper).toMatchSnapshot();
    });

    it('renders correctly(fail)', () => {
        store = mockStore({
            ...initialState,
            pender: penderFailure
        });
        const wrapper = shallow(<PlanWizard
            dispatch={dispatch}
            store={store}
            isNew = {true}
            editId = {0}
        />).dive().dive();
        expect(wrapper).toMatchSnapshot();
    });

    it('call handleRequestAutoPlanAdd', () => {

        services.axiosAPI.requestPost = jest.fn().mockResolvedValue({
            data: {
                id: 1,
            },
        })

        store = mockStore(initialState);
        const wrapper = shallow(<PlanWizard
            dispatch={dispatch}
            store={store}
            isNew = {true}
            editId = {0}
        />).dive().dive();
        wrapper.instance().handleRequestAutoPlanAdd();
    });

    it('call handleRequestAutoPlanEdit', () => {

        services.axiosAPI.requestPost = jest.fn().mockResolvedValue({
            data: {
                id: 2,
            },
        })

        store = mockStore(initialState);
        const wrapper = shallow(<PlanWizard
            dispatch={dispatch}
            store={store}
            isNew = {false}
            editId = {1}
        />).dive().dive();
        wrapper.instance().handleRequestAutoPlanEdit();
    });

    it('drawPrevButton, drawNextButton ', () => {
        store = mockStore(initialState);
        const wrapper = shallow(<PlanWizard
            dispatch={dispatch}
            store={store}
            isNew = {true}
            editId = {0}
        />).dive().dive();
        wrapper.setState({
            currentStep: wizardStep.CHECK
        })

        wrapper.setState({
            isNew: false
        })

        wrapper.setState({
            currentStep: 100
        })


    });

    it('handleNext', () => {
        store = mockStore({
            ...initialState,
            viewList: {
                get: () => 0
            }
        });
        let wrapper = shallow(<PlanWizard
            dispatch={dispatch}
            store={store}
            isNew={true}
            editId={0}
        />).dive().dive();
        wrapper.instance().handleNext();
    })

    it('handleNext2', () => {
        store = mockStore(initialState);
        let wrapper = shallow(<PlanWizard
            dispatch={dispatch}
            store={store}
            isNew = {true}
            editId = {0}
        />).dive().dive();
        wrapper.setState({ currentStep: wizardStep.CHECK })
        wrapper.instance().handleNext();

        wrapper.setState({ currentStep: wizardStep.CHECK, isNew: false })
        wrapper.instance().handleNext();

        wrapper.setState({ currentStep: wizardStep.OPTION})
        wrapper.instance().handleNext();
    })

    it('handlePrev ', () => {
        store = mockStore(initialState);
        let wrapper = shallow(<PlanWizard
            dispatch={dispatch}
            store={store}
            isNew={true}
            editId={0}
        />).dive().dive();
        wrapper.setState({
            completeStep: [wizardStep.MACHINE , wizardStep.TARGET_COMMAND]
        })
        wrapper.instance().handlePrev();
        wrapper.setState({ currentStep: wizardStep.TARGET_COMMAND })
        wrapper.instance().handlePrev();
    });

    it('calculateTime  ', () => {
        store = mockStore(initialState);
        let wrapper = shallow(<PlanWizard
            dispatch={dispatch}
            store={store}
            isNew={true}
            editId={0}
        />).dive().dive();

        wrapper.instance().calculateTime(Define.AUTO_MODE_CONTINUOUS, "", 0);
        wrapper.instance().calculateTime(Define.AUTO_MODE_CYCLE, "10", Define.AUTO_UNIT_MINUTE);
        wrapper.instance().calculateTime(Define.AUTO_MODE_CYCLE, "10", Define.AUTO_UNIT_HOUR);
        wrapper.instance().calculateTime(Define.AUTO_MODE_CYCLE, "10", Define.AUTO_UNIT_DAY);
    });

    it('modalOpen, modalClose', () => {
        store = mockStore(initialState);
        let wrapper = shallow(<PlanWizard
            dispatch={dispatch}
            store={store}
            isNew={true}
            editId={0}
        />).dive().dive();

        wrapper.instance().modalOpen(0, "test");
        wrapper.instance().modalOpen(modalType.ALERT, "test");
        wrapper.instance().modalOpen(modalType.CONFIRM, "test");

        wrapper.instance().modalClose();
    });

    it('invalidCheck', () => {
        let initCommand = commandInit;
        let initOptionsList = Map({
            planId: "",
            collectStart: moment().startOf('day'),
            from: moment().startOf('day'),
            to : moment().endOf('day'),
            collectType: Define.AUTO_MODE_CONTINUOUS,
            interval: 1,
            intervalUnit: Define.AUTO_UNIT_MINUTE,
            description: ""
        })
        let optionList;

        // step, toolCnt, targetCnt, optionList
        invalidCheck(wizardStep.MACHINE, 0, 0, null);
        invalidCheck(wizardStep.MACHINE, 1, 0, null);

        invalidCheck(wizardStep.TARGET_COMMAND, 0, 0, null, Define.PLAN_TYPE_FTP, null);
        invalidCheck(wizardStep.TARGET_COMMAND, 0, 1, null, Define.PLAN_TYPE_FTP, null);
        invalidCheck(wizardStep.TARGET_COMMAND, 0, 0, null, Define.PLAN_TYPE_VFTP_COMPAT, initCommand);
        invalidCheck(wizardStep.TARGET_COMMAND, 0, 0, null, Define.PLAN_TYPE_VFTP_SSS, initCommand);

        optionList = initOptionsList.set("planId", "######");
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
                                    .set("from", moment().endOf('day'))
                                    .set("to", moment().startOf('day'));
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
                                    .set("collectType", Define.AUTO_MODE_CYCLE)
                                    .set("interval", 0);
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
                                    .set("collectType", Define.AUTO_MODE_CYCLE);
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
                                    .set("collectType", Define.AUTO_MODE_CYCLE)
                                    .set("interval", 60);
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
                                    .set("collectType", Define.AUTO_MODE_CYCLE)
                                    .set("intervalUnit", Define.AUTO_UNIT_HOUR);
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
                                    .set("collectType", Define.AUTO_MODE_CYCLE)
                                    .set("intervalUnit", Define.AUTO_UNIT_HOUR)
                                    .set("interval", 24);
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
                                    .set("collectType", Define.AUTO_MODE_CYCLE)
                                    .set("intervalUnit", Define.AUTO_UNIT_DAY);
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
                                    .set("collectType", Define.AUTO_MODE_CYCLE)
                                    .set("intervalUnit", Define.AUTO_UNIT_DAY)
                                    .set("interval", 366);
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
                                    .set("collectType", Define.AUTO_MODE_CYCLE)
                                    .set("intervalUnit", "null");
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
                                    .set("description", "test1234");
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);

        optionList = initOptionsList.set("planId", "test")
            .set("description", "###test1234");
        invalidCheck(wizardStep.OPTION, 0, 0, optionList);
    });
});
