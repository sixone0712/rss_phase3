import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import Auto from "../Auto";
import { CreateBreadCrumb, writePlanMessage } from '../Auto';
import sinon from "sinon";

import * as Define from '../../../define';

const initProps = {
    location: {
        pathname: Define.PAGE_AUTO_PLAN_ADD
    }
}

describe('Auto', () => {

    let props;

    beforeEach(() => {
        props = initProps;
    });

    it('renders when page is Define.PAGE_AUTO_PLAN_ADD', async () => {
        const wrapper = shallow(<Auto {...props}/>);
        expect(wrapper).toMatchSnapshot();
    });

    it('renders when page is Define.AUTO_CUR_PAGE_STATUS', () => {
        props.location.pathname = Define.PAGE_AUTO_STATUS;
        const wrapper = shallow(<Auto {...props}/>);
        expect(wrapper).toMatchSnapshot();
    });

    it('renders when page is Define.PAGE_AUTO_DOWNLOAD', () => {
        props.location.pathname = Define.PAGE_AUTO_DOWNLOAD;
        const wrapper = shallow(<Auto {...props}/>);
        expect(wrapper).toMatchSnapshot();
    });

    it('renders when page is Define.PAGE_AUTO_PLAN_EDIT', () => {
        props.location.pathname = Define.PAGE_AUTO_PLAN_EDIT;
        const wrapper = shallow(<Auto {...props}/>);
        expect(wrapper).toMatchSnapshot();
    });

    it('call func CreateBreadCrumb', () => {
        CreateBreadCrumb({page: Define.AUTO_CUR_PAGE_ADD});
        CreateBreadCrumb({page: Define.AUTO_CUR_PAGE_STATUS});
        CreateBreadCrumb({page: Define.AUTO_CUR_PAGE_EDIT});
        CreateBreadCrumb({page: Define.AUTO_CUR_PAGE_DOWNLOAD});
        CreateBreadCrumb({page: 5});
    })

    it('call func writePlanMessage', () => {
       writePlanMessage(Define.PLAN_TYPE_FTP);
       writePlanMessage(Define.PLAN_TYPE_VFTP_COMPAT);
       writePlanMessage(Define.PLAN_TYPE_VFTP_SSS);
       writePlanMessage("invalid type");
    });
});
