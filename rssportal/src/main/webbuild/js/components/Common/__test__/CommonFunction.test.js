import 'babel-polyfill';
import React from 'react';
import { mount } from 'enzyme';
import { filePaginate, RenderPagination, propsCompare, stringBytes, invalidCheckVFTP, setCurrentCommand } from "../CommonFunction";
import * as Define from "../../../define";

const data = [
    { name: "dummy", index: 0 },
    { name: "dummy", index: 1 },
    { name: "dummy", index: 2 },
    { name: "dummy", index: 3 },
    { name: "dummy", index: 4 },
    { name: "dummy", index: 5 },
    { name: "dummy", index: 6 },
    { name: "dummy", index: 7 },
    { name: "dummy", index: 8 },
    { name: "dummy", index: 9 },
    { name: "dummy", index: 10 },
    { name: "dummy", index: 11 },
    { name: "dummy", index: 12 },
    { name: "dummy", index: 13 },
    { name: "dummy", index: 14 }
];

const nextData = [
    { name: "dummy", index: 1 },
    { name: "dummy", index: 2 },
    { name: "dummy", index: 3 },
    { name: "dummy", index: 4 },
    { name: "dummy", index: 5 },
    { name: "dummy", index: 6 },
    { name: "dummy", index: 7 },
    { name: "dummy", index: 8 },
    { name: "dummy", index: 9 },
    { name: "dummy", index: 10 },
    { name: "dummy", index: 11 },
    { name: "dummy", index: 12 },
    { name: "dummy", index: 13 },
    { name: "dummy", index: 14 },
    { name: "dummy", index: 15 }
];

const MAX_BYTES_OVER_STRING = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901";

const handleClick = jest.fn();

const initProps = {
    pageSize: 10,
    itemsCount: 100,
    onPageChange: jest.fn(),
    currentPage: 1,
    className: "custom-pagination"
};

describe("CommonFunction test", () => {
    it("Check filePaginate", () => {
        filePaginate(data, 2, 10);
    });

    it("Renders when value of all props is correct", () => {
        const component = mount(<RenderPagination {...initProps} />);
        expect(component).toMatchSnapshot();
    });

    it("Renders when pageSize and itemsCount are the same", () => {
        const newProps = {
            ...initProps,
            itemsCount: 10
        }
        const component = mount(<RenderPagination {...newProps} />);
        expect(component).toMatchSnapshot();
    });

    it("Check onClick event", () => {
        const component = mount(
            <RenderPagination
                pageSize={initProps.pageSize}
                itemsCount={initProps.itemsCount}
                onPageChange={handleClick}
                currentPage={initProps.currentPage}
                className={initProps.className}
            />
        );
        const button = component.find('button').at(7);
        button.simulate('click');
        expect(handleClick).toHaveBeenCalled();
    });

    it("Check propsCompare", () => {
        propsCompare(data, nextData);
    });

    it("Check stringBytes", () => {
        stringBytes("test string");
        stringBytes("한글 테스트");
        stringBytes("Ø");
    });

    it("Check invalidCheckVFTP", () => {
        invalidCheckVFTP(Define.PLAN_TYPE_VFTP_COMPAT, jest.fn(), () => jest.fn(), "TEST", "");
        invalidCheckVFTP(Define.PLAN_TYPE_VFTP_COMPAT, jest.fn(), () => jest.fn(), MAX_BYTES_OVER_STRING, "");
        invalidCheckVFTP(Define.PLAN_TYPE_VFTP_COMPAT, jest.fn(), () => jest.fn(), "", "");
        invalidCheckVFTP(Define.PLAN_TYPE_VFTP_SSS, jest.fn(), () => jest.fn(), "", "");
    });

    it("Check setCurrentCommand", () => {
        setCurrentCommand(Define.PLAN_TYPE_VFTP_COMPAT, "", "TEST");
        setCurrentCommand(Define.PLAN_TYPE_VFTP_SSS, "TEST", "TEST");
        setCurrentCommand(Define.PLAN_TYPE_VFTP_SSS, "TEST", "");
    });
});