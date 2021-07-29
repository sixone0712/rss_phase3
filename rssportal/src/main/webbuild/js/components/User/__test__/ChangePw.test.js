import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import { Map } from 'immutable';
import ChangePw from "../ChangePw";
import sinon from "sinon";
import * as Define from "../../../define";
import * as LoginAPI from "../../../api/Login";

const initialStore = {
    login: {
        get: (id) => {
            switch (id) {
                case "loginInfo":
                    return Map({
                        errCode: 0,
                        isLoggedIn: true,
                        username: "chpark",
                        password: "",
                        auth: "100",
                    })
                default: return jest.fn();
            }
        }
    },
};

const mockStore = configureStore();
const dispatch = sinon.spy();
let store;
const initProps = {
    isOpen: jest.fn(),
    right: jest.fn(),
    alertOpen: jest.fn()
};

describe('ChangePw', () => {
    it('renders correctly', () => {
        store = mockStore(initialStore);
        const wrapper = shallow(<ChangePw
            dispatch={dispatch}
            store={store}
            {...initProps}
        />).dive().dive();
        expect(wrapper).toMatchSnapshot();
    });

    it('renders correctly(other conditions) 1', () => {
        store = mockStore(initialStore);
        const wrapper = shallow(<ChangePw
            dispatch={dispatch}
            store={store}
            props={{
                ...initProps,
                isOpen: false
            }}
        />).dive().dive();
        expect(wrapper).toMatchSnapshot();
    });

    it('renders correctly(other conditions) 2', () => {
        store = mockStore(initialStore);
        const wrapper = shallow(<ChangePw
            dispatch={dispatch}
            store={store}
            props={{
                ...initProps,
                isOpen: true
            }}
        />).dive().dive();
        expect(wrapper).toMatchSnapshot();
    });


    it('handleSubmit', () => {
        store = mockStore(initialStore);
        const wrapper = shallow(<ChangePw
            dispatch={dispatch}
            store={store}
            {...initProps}
        />).dive().dive();
        wrapper.instance().handleSubmit();

        wrapper.setState({
            oldPw: "111111",
            newPw: "!@#$",
            confirmPw: "!@#$"
        });
        wrapper.instance().handleSubmit();

        wrapper.setState({
            oldPw: "111111",
            newPw: "123456",
            confirmPw: "1234567"
        })
        wrapper.instance().handleSubmit();

        wrapper.setState({
            oldPw: "111111",
            newPw: "123456",
            confirmPw: "123456"
        })
        wrapper.instance().handleSubmit();
    });

    it('changePwProcess', async () => {
        const newStore = {
            login: {
                get: (id) => {
                    switch (id) {
                        case "loginInfo":
                            return Map({
                                errCode: 0,
                                isLoggedIn: true,
                                username: "chpark",
                                password: "",
                                auth: "100",
                            })
                        default: return jest.fn();
                    }
                }
            },
        }
        store = mockStore(newStore);
        const wrapper = shallow(<ChangePw
            dispatch={dispatch}
            store={store}
            {...initProps}
        />).dive().dive();

        wrapper.setState({
            oldPw: "111111",
            newPw: "123456",
            confirmPw: "1234567"
        })
        await wrapper.instance().changePwProcess();

        LoginAPI.changePassword = jest.fn().mockReturnValue(true);
        LoginAPI.getErrCode = jest.fn().mockReturnValue(Define.CHANGE_PW_FAIL_INCORRECT_CURRENT_PASSWORD);
        wrapper.setState({
            oldPw: "111111",
            newPw: "123456",
            confirmPw: "123456"
        })
        await wrapper.instance().changePwProcess();

        LoginAPI.getErrCode = jest.fn().mockReturnValue(Define.RSS_SUCCESS);
        await wrapper.instance().changePwProcess();
    });

    it('changeHandler, closeModal)', () => {
        store = mockStore(initialStore);
        const wrapper = shallow(<ChangePw
            dispatch={dispatch}
            store={store}
            {...initProps}
        />).dive().dive();

        const e = {
            target: {
                name: "password",
                value: "1234546"
            }
        }
        wrapper.instance().changeHandler(e);
        wrapper.instance().closeModal();
    });
});