import 'babel-polyfill';
import React from "react";
import { mount } from "enzyme";
import NetworkError from "../NetworkError";
import Footer from "../Footer";

describe('Common Network Error test', () => {
    it('Snapshot check', () => {
        const component = mount(<NetworkError />);
        expect(component).toMatchSnapshot();
    });
});