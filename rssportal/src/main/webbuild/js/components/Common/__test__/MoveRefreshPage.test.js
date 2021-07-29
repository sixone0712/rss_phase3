import 'babel-polyfill';
import React from 'react';
import configureStore from 'redux-mock-store'
import { shallow } from 'enzyme';
import MoveRefreshPage from "../MoveRefreshPage";
import sinon from "sinon";

import * as Define from '../../../define';

const initialStore = {
    viewList: {
        get: jest.fn()
    },
    autoPlan: {
        get: jest.fn()
    },
}
const mockStore = configureStore();
const dispatch = sinon.spy();
let store;
const initProps = {
    location: {
        search: `?target=${Define.PAGE_AUTO_PLAN_EDIT}`
    },
    history: {
        replace: jest.fn()
    }
}

describe('PAGE_AUTO_PLAN_EDIT', () => {
    it('renders correctly', () => {
        store = mockStore(initialStore);
        const wrapper = shallow(<MoveRefreshPage
            dispatch={dispatch}
            store={store}
            isNew = {true}
            {...initProps}
        />).dive().dive();
    });

    it('PAGE_AUTO_PLAN_ADD', () => {
        store = mockStore(initialStore);
        const newProps = {
            ...initProps,
            location: {
                search: `?target=${Define.PAGE_AUTO_PLAN_ADD}?type=${Define.PLAN_TYPE_FTP}`
            }
        }
        const wrapper = shallow(<MoveRefreshPage
            dispatch={dispatch}
            store={store}
            {...newProps}
        />).dive().dive();
    });

    it('PAGE_MANUAL', () => {
        store = mockStore(initialStore);
        const newProps = {
            ...initProps,
            location: {
                search: `?target=${Define.PAGE_MANUAL_FTP}`
            }
        }
        const wrapper = shallow(<MoveRefreshPage
            dispatch={dispatch}
            store={store}
            {...newProps}
        />).dive().dive();
    });

    it('PAGE_MANUAL(COMPAT)', () => {
        store = mockStore(initialStore);
        const newProps = {
            ...initProps,
            location: {
                search: `?target=${Define.PAGE_MANUAL_VFTP_COMPAT}`
            }
        }
        const wrapper = shallow(<MoveRefreshPage
            dispatch={dispatch}
            store={store}
            {...newProps}
        />).dive().dive();
    });

    it('PAGE_MANUAL(SSS)', () => {
        store = mockStore(initialStore);
        const newProps = {
            ...initProps,
            location: {
                search: `?target=${Define.PAGE_MANUAL_VFTP_SSS}`
            }
        }
        const wrapper = shallow(<MoveRefreshPage
            dispatch={dispatch}
            store={store}
            {...newProps}
        />).dive().dive();
    });

    it('PAGE_AUTO_STATUS', () => {
        store = mockStore(initialStore);
        const newProps = {
            ...initProps,
            location: {
                search: `?target=${Define.PAGE_AUTO_STATUS}`
            }
        }
        const wrapper = shallow(<MoveRefreshPage
            dispatch={dispatch}
            store={store}
            {...newProps}
        />).dive().dive();
    });

    it('PAGE_ADMIN_ACCOUNT', () => {
        store = mockStore(initialStore);
        const newProps = {
            ...initProps,
            location: {
                search: `?target=${Define.PAGE_ADMIN_ACCOUNT}`
            }
        }
        const wrapper = shallow(<MoveRefreshPage
            dispatch={dispatch}
            store={store}
            {...newProps}
        />).dive().dive();
    });

    it('PAGE_ADMIN_DL_HISTORY', () => {
        store = mockStore(initialStore);
        const newProps = {
            ...initProps,
            location: {
                search: `?target=${Define.PAGE_ADMIN_DL_HISTORY}`
            }
        }
        const wrapper = shallow(<MoveRefreshPage
            dispatch={dispatch}
            store={store}
            {...newProps}
        />).dive().dive();
    });

    it('PAGE_DEFAULT', () => {
        store = mockStore(initialStore);
        const newProps = {
            ...initProps,
            location: {
                search: `?target=${Define.PAGE_DEFAULT}`
            }
        }
        const wrapper = shallow(<MoveRefreshPage
            dispatch={dispatch}
            store={store}
            {...newProps}
        />).dive().dive();
    });

    it('exception', () => {
        store = mockStore(initialStore);
        const newProps = {
            ...initProps,
            location: {
                search: `?target=`
            }
        }
        const wrapper = shallow(<MoveRefreshPage
            dispatch={dispatch}
            store={store}
            {...newProps}
        />).dive().dive();
    });
});
