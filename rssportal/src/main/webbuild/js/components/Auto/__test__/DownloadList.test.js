import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import { Map } from 'immutable';
import sinon from "sinon";
import moment from "moment";
import services from '../../../services';

import DownloadList from "../DownloadList";
import { statusType, modalType, CreateStatus } from "../DownloadList";
import * as Define from "../../../define";

services.axiosAPI.requestDelete = jest.fn().mockResolvedValue();
services.axiosAPI.requestGet = jest.fn().mockResolvedValue({
    data: {
        lists: [
            {
                created: "2020-08-24 05:23:22.895000",
                status: "finished",
                fileId: 3,
                planId: 23,
                downloadUrl: "",
                idx: 0
            },
            {
                created: "2020-08-24 06:23:22.895000",
                status: "finished",
                fileId: 4,
                planId: 23,
                downloadUrl: "/rss/api/plans/storage/4",
                idx: 1
            },
            {
                created: "2020-08-24 08:23:22.895000",
                status: "new",
                fileId: 5,
                planId: 23,
                downloadUrl: "/rss/api/plans/storage/5",
                idx: 2
            },
            {
                created: "2020-08-24 18:23:22.895000",
                status: "new",
                fileId: 6,
                planId: 23,
                downloadUrl: "/rss/api/plans/storage/6",
                idx: 3
            }
        ]
    }
});

describe('DownloadList', () => {
    const props = {
        location: {
            search: "?id=235&name=test1"
        }
    }

    it('renders correctly(there is downloadlist)', () => {
        const wrapper = shallow(<DownloadList {...props} />)
        wrapper.setState({
            requestList: [
                {
                    created: "2020-08-24 05:23:22.895000",
                    status: "finished",
                    fileId: 3,
                    planId: 23,
                    downloadUrl: "",
                    idx: 0
                },
                {
                    created: "2020-08-24 06:23:22.895000",
                    status: "finished",
                    fileId: 4,
                    planId: 23,
                    downloadUrl: "/rss/api/plans/storage/4",
                    idx: 1
                },
                {
                    created: "2020-08-24 08:23:22.895000",
                    status: "new",
                    fileId: 5,
                    planId: 23,
                    downloadUrl: "/rss/api/plans/storage/5",
                    idx: 2
                },
                {
                    created: "2020-08-24 18:23:22.895000",
                    status: "new",
                    fileId: 6,
                    planId: 23,
                    downloadUrl: "/rss/api/plans/storage/6",
                    idx: 3
                }
            ]
        })
        expect(wrapper).toMatchSnapshot();

        wrapper.find('tbody').find('tr').at(0).find('.request-id-area').simulate('click');
        wrapper.find('tbody').find('tr').at(0).find('.icon-area').simulate('click');
        wrapper.find('.content-section').find('Button').dive().simulate('click');
        //console.log(wrapper.find('.content-section').find('Button').dive().debug());
    });

    it('renders correctly(there is not downloadlist)', () => {
        services.axiosAPI.requestGet = jest.fn().mockResolvedValue({ data: ""});
        const wrapper = shallow(<DownloadList {...props} />)
        expect(wrapper).toMatchSnapshot();
    });

    it('openModal, closeModal', () => {
        const wrapper = shallow(<DownloadList {...props} />)
        wrapper.instance().openModal(modalType.MODAL_DELETE);
        wrapper.instance().openModal(modalType.MODAL_DOWNLOAD_1);
        wrapper.instance().openModal(modalType.MODAL_DOWNLOAD_2);
        wrapper.instance().openModal(modalType.MODAL_ALERT);
        wrapper.instance().openModal(modalType.MODAL_NETWORK_ERROR);
        wrapper.instance().openModal(modalType.MODAL_FILE_NOT_FOUND);
        wrapper.instance().openModal(-1);
        wrapper.instance().closeModal();
    });

    it('handlePaginationChange', () => {
        const wrapper = shallow(<DownloadList {...props} />)
        wrapper.instance().handlePaginationChange(0);
    });

    it('handleSelectBoxChange ', () => {
        const wrapper = shallow(<DownloadList {...props} />)
        wrapper.instance().handleSelectBoxChange(10);

        wrapper.setState({
            pageSize: 2,
            currentPage: 2,
        })
        wrapper.instance().handleSelectBoxChange(10);
    });

    it('checkNewDownloadFile  ', () => {
        const wrapper = shallow(<DownloadList {...props} />)

        wrapper.setState({
            requestList: [{
                created: '2020-08-24 05:23:22.895000',
                status: 'new',
                fileId: 3,
                planId: 23,
                downloadUrl: '/rss/api/plans/storage/3',
                keyIndex: 1
            }]
        });
        wrapper.instance().checkNewDownloadFile();

        wrapper.setState({
            requestList: [{
                created: '2020-08-24 05:23:22.895000',
                status: 'finished',
                fileId: 3,
                planId: 23,
                downloadUrl: '/rss/api/plans/storage/3',
                keyIndex: 1
            }]
        });
        wrapper.instance().checkNewDownloadFile();
    });

    it('saveDownloadFile (RSS_SUCCESS)', () => {
        const wrapper = shallow(<DownloadList {...props} />)
        wrapper.setState({
            download: {
                created: '2020-08-24 05:23:22.895000',
                status: 'new',
                fileId: 3,
                planId: 23,
                downloadUrl: '/rss/api/plans/storage/3',
                keyIndex: 1
            }
        });
        wrapper.instance().saveDownloadFile();

        services.axiosAPI.downloadFile = jest.fn().mockResolvedValue({
            result: Define.RSS_SUCCESS,
            fileName: "chpark_Fab1_20200601_155410.zip"
        });
        wrapper.instance().saveDownloadFile();
    });

    it('saveDownloadFile (COMMON_FAIL_NOT_FOUND)', () => {
        const wrapper = shallow(<DownloadList {...props} />)
        wrapper.setState({
            download: {
                created: '2020-08-24 05:23:22.895000',
                status: 'new',
                fileId: 3,
                planId: 23,
                downloadUrl: '/rss/api/plans/storage/3',
                keyIndex: 1
            }
        });

        services.axiosAPI.downloadFile = jest.fn().mockResolvedValue({
            result: Define.COMMON_FAIL_NOT_FOUND,
            fileName: ""
        });
        wrapper.instance().saveDownloadFile();
    });

    it('saveDownloadFile (FAIL)', () => {
        const wrapper = shallow(<DownloadList {...props} />)
        wrapper.setState({
            download: {
                created: '2020-08-24 05:23:22.895000',
                status: 'new',
                fileId: 3,
                planId: 23,
                downloadUrl: '/rss/api/plans/storage/3',
                keyIndex: 1
            }
        });

       services.axiosAPI.downloadFile = jest.fn().mockResolvedValue({
           result: Define.RSS_FAIL,
           fileName: ""
       });
       wrapper.instance().saveDownloadFile();
    });

    it('saveDownloadFile (no downloadUrl)', () => {
        const wrapper = shallow(<DownloadList {...props} />)
        wrapper.setState({
            download: {
                created: '2020-08-24 05:23:22.895000',
                status: 'new',
                fileId: 3,
                planId: 23,
                downloadUrl: '',
                keyIndex: 1
            }
        });
        wrapper.instance().saveDownloadFile();
    });

    it('deleteDownloadFile, requestDelete',async () => {
        const wrapper = shallow(<DownloadList {...props} />);
        wrapper.setState({
            delete: {
                created: '2020-08-24 05:23:22.895000',
                status: 'new',
                fileId: 3,
                planId: 23,
                downloadUrl: '/rss/api/plans/storage/3',
                keyIndex: 2
            }
        });
        services.axiosAPI.requestGet = jest.fn().mockResolvedValue(Define.RSS_SUCCESS);
        await wrapper.instance().deleteDownloadFile();

        wrapper.setState({
            delete: {
                created: '2020-08-24 05:23:22.895000',
                status: 'new',
                fileId: 3,
                planId: 23,
                downloadUrl: '/rss/api/plans/storage/3',
                keyIndex: 1
            }
        });
        services.axiosAPI.requestGet = jest.fn().mockResolvedValue(Define.RSS_SUCCESS);
        await wrapper.instance().deleteDownloadFile();

        wrapper.setState({
            delete: {
                created: '2020-08-24 05:23:22.895000',
                status: 'new',
                fileId: "",
                planId: 23,
                downloadUrl: '/rss/api/plans/storage/3',
                keyIndex: 1
            }
        });
        await wrapper.instance().deleteDownloadFile();
    });
});

describe('CreateStatus', () => {
    it('CreateStatus', () => {
        CreateStatus(statusType.STATUS_NEW);
        CreateStatus(statusType.STATUS_FINISHED);
        CreateStatus("");
    });
});