import { shallow } from 'enzyme';
import React from 'react';
import LogSider from '../../../../components/Dashboard/System/LogDownload/LogSider';
import * as DashboardContext from '../../../../contexts/DashboardContext';

const receivedList = [
  {
    key: 0,
    type: 'ESP',
    name: 'ESP',
    ip: '',
    status: ['Database (Up 2 hours)', 'Rapid-Collector (Up 2 hours)'],
    volume: '0.1G / 163.0G',
  },
  {
    key: 0,
    type: 'OTS',
    name: 'OTS_DEV',
    ip: '10.1.31.228',
    status: ['FileServiceCollect (Up 2 hours)'],
    volume: '0.1G / 163.0G',
  },
];

describe('renders the component', () => {
  beforeAll(async done => {
    done();
  });
  it('renders correctly', () => {
    const mocked_useDashBoardState = jest.spyOn(
      DashboardContext,
      'useDashBoardState',
    );
    mocked_useDashBoardState.mockImplementation(() => ({
      deviceInfo: {
        list: receivedList,
        success: false,
        error: false,
        failure: false,
        pending: false,
        selected: 'ESP',
      },
    }));
    const mocked_useDashBoardDispatch = jest.spyOn(
      DashboardContext,
      'useDashBoardDispatch',
    );
    mocked_useDashBoardDispatch.mockImplementation(jest.fn);
    const component = shallow(<LogSider />);

    component
      .find('Menu')
      .getElement()
      .props.onClick({ item: null, key: 1, keyPath: null, domEvent: null });
  });

  it('selected is null', () => {
    const mocked_useDashBoardState = jest.spyOn(
      DashboardContext,
      'useDashBoardState',
    );
    mocked_useDashBoardState.mockImplementation(() => ({
      deviceInfo: {
        list: receivedList,
        success: false,
        error: false,
        failure: false,
        pending: false,
        selected: null,
      },
    }));
    const mocked_useDashBoardDispatch = jest.spyOn(
      DashboardContext,
      'useDashBoardDispatch',
    );
    mocked_useDashBoardDispatch.mockImplementation(jest.fn);
    const component = shallow(<LogSider />);
  });
});
