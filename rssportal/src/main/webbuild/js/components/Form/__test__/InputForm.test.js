import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import InputForm from "../InputForm";
import sinon from "sinon";

const initProps = {
    iType: "test",
    iLabel: "test",
    iName: "test",
    iId: "test",
    iPlaceholder: "test",
    changeFunc: jest.fn(),
    iErrMsg: "test",
    maxLength: 0
};

const nullProps = {
    iType: "test",
    iLabel: "test",
    iName: "test",
    iId: "test",
    iPlaceholder: "test",
    changeFunc: jest.fn(),
    iErrMsg: "",
    maxLength: 0
};

const undefinedProps = {
    iType: "test",
    iLabel: "test",
    iName: "test",
    iId: "test",
    iPlaceholder: "test",
    changeFunc: jest.fn(),
    iErrMsg: undefined,
    maxLength: 0
};

describe('DateForm', () => {
    it('renders correctly', () => {
        const wrapper = shallow(<InputForm
            {...initProps}
        />);
        expect(wrapper).toMatchSnapshot();
    });

    it('renders correctly when iErrMsg is null', () => {
        const wrapper = shallow(<InputForm
            {...nullProps}
        />);
        expect(wrapper).toMatchSnapshot();
    });

    it('renders correctly when iErrMsg is undefined', () => {
        const wrapper = shallow(<InputForm
            {...undefinedProps}
        />);
        expect(wrapper).toMatchSnapshot();
    });
});