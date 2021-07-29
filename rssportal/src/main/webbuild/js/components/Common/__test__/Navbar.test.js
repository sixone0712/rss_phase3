import 'babel-polyfill';
import React from 'react';
import { shallow } from 'enzyme';
import configureStore from 'redux-mock-store'
import Navbar from "../Navbar";
import sinon from "sinon";
import { Map } from "immutable";
import axios from "axios";

const initProps = {
    onMovePage: jest.fn()
};

const adminStore = {
    login: {
        get: () => {
            return Map({
                errCode: 0,
                isLoggedIn: true,
                username: "gtpark",
                password: "827ccb0eea8a706c4c34a16891f84e7b",
                auth: "100"
            })
        }
    }
};

const userStore = {
    login: {
        get: () => {
            return Map({
                errCode: 0,
                isLoggedIn: true,
                username: "user",
                password: "827ccb0eea8a706c4c34a16891f84e7b",
                auth: "10"
            })
        }
    }
};

const initialState = {
    currentPage: "Manual",
    isPasswordOpen : false,
    isLogoutOpen : false,
    isAlertOpen: false,
    isPlanOpen: false,
    alertMessage: ""
};

const mockStore = configureStore();
const dispatch = sinon.spy();
let store;

describe("Common Navbar test", () => {
    it("Renders when value of all props is correct(admin)", () => {
        store = mockStore(adminStore);
        const component = shallow(<Navbar {...initProps} dispatch={dispatch} store={store} />).dive().dive();
        //console.log(component.find('UncontrolledDropdown').at(0).find('DropdownItem').at(4).dive().debug());
        expect(component).toMatchSnapshot();

        component.find('UncontrolledDropdown').at(0).find('DropdownItem').at(0).simulate('click');
        component.find('UncontrolledDropdown').at(0).find('DropdownItem').at(2).simulate('click');
        component.find('UncontrolledDropdown').at(0).find('DropdownItem').at(4).simulate('click');

        component.find('UncontrolledDropdown').at(1).find('DropdownItem').at(0).simulate('click');
        component.find('UncontrolledDropdown').at(1).find('DropdownItem').at(2).simulate('click');

        component.find('UncontrolledDropdown').at(2).find('DropdownItem').at(0).simulate('click');
        component.find('UncontrolledDropdown').at(2).find('DropdownItem').at(2).simulate('click');

        component.find('UncontrolledDropdown').at(3).find('DropdownItem').at(0).simulate('click');
        component.find('UncontrolledDropdown').at(3).find('DropdownItem').at(2).simulate('click');
    });

    it("Renders when value of all props is correct(user)", () => {
        store = mockStore(userStore);
        const component = shallow(<Navbar {...initProps} dispatch={dispatch} store={store} />).dive().dive();
        expect(component).toMatchSnapshot();
    });

    it("Check getClassName method", () => {
        store = mockStore(adminStore);
        const component = shallow(<Navbar {...initProps} dispatch={dispatch} store={store} />).dive().dive();
        expect(component.instance().getClassName("Auto")).toEqual(null);
        expect(component.instance().getClassName("Manual")).toEqual("nav-item-custom-select");
    });

    it("Check handlePageChange method", () => {
        store = mockStore(adminStore);
        const component = shallow(<Navbar {...initProps} dispatch={dispatch} store={store} />).dive().dive();
        component.instance().handlePageChange("admin");
        expect(component.state().currentPage).toEqual("admin");
    });

    it("Check openModal/closeModal method", () => {
        store = mockStore(adminStore);
        const component = shallow(<Navbar {...initProps} dispatch={dispatch} store={store} />).dive().dive();
        component.instance().openModal("test");
        expect(component.state().isPasswordOpen).toEqual(false);
        expect(component.state().isLogoutOpen).toEqual(false);
        //expect(component.state().isMode).toEqual("");
        component.instance().openModal("password");
        expect(component.state().isPasswordOpen).toEqual(true);
        //expect(component.state().isMode).toEqual("password");
        component.instance().openModal("logout");
        expect(component.state().isLogoutOpen).toEqual(true);
        //expect(component.state().isMode).toEqual("logout");
        component.instance().openModal("plan");
        expect(component.state().isPlanOpen).toEqual(true);
        component.find('PlanModal').dive().find('Button').at(0).simulate('click');
        component.instance().openModal("plan");
        component.find('PlanModal').dive().find('Button').at(1).simulate('click');
        component.instance().openModal("plan");
        component.find('PlanModal').dive().find('Button').at(2).simulate('click');
        component.instance().closeModal();
        component.find('PlanModal').dive();
        expect(component.state().isPasswordOpen).toEqual(false);
        expect(component.state().isLogoutOpen).toEqual(false);
        expect(component.state().isPlanOpen).toEqual(false);
        //expect(component.state().isMode).toEqual("");
    });

    it("Check openAlert/closeAlert method", () => {
        store = mockStore(adminStore);
        const component = shallow(<Navbar {...initProps} dispatch={dispatch} store={store} />).dive().dive();
        jest.useFakeTimers();
        component.instance().openAlert("test");
        jest.advanceTimersByTime(200);
        jest.useRealTimers();
        expect(component.state().isAlertOpen).toEqual(false);
        expect(component.state().alertMessage).toEqual("");
        jest.useFakeTimers();
        component.instance().openAlert("password");
        jest.advanceTimersByTime(200);
        jest.useRealTimers();
        expect(component.state().isAlertOpen).toEqual(true);
        expect(component.state().alertMessage).toEqual("Password change completed.");
        component.instance().closeAlert();
        expect(component.state().isAlertOpen).toEqual(false);
        expect(component.state().alertMessage).toEqual("");
    });

    it("Check onLogout method", async () => {
        store = mockStore(adminStore);
        axios.get = jest.fn().mockResolvedValue();
        const component = shallow(<Navbar {...initProps} dispatch={dispatch} store={store} />).dive().dive();
        component.setProps({
            onMovePage: jest.fn()
        })
        await component.instance().onLogout();
    });
});