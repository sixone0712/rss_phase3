import { shallow } from 'enzyme';
import React from 'react';
import StatusTable, {
  StatusColor,
} from '../../../components/Dashboard/System/StatusTable';
// import { useDashBoardState, useDashBoardDispatch } from '../../../contexts/DashboardContext';
import * as DashboardContext from '../../../contexts/DashboardContext';

const initialDeviceInfo = {
  deviceInfo: {
    list: [],
    success: false,
    error: false,
    failure: false,
    pending: false,
    selected: null,
  },
};

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

// jest.mock('../../../contexts/DashboardContext.tsx', () => ({
//   ...jest.requireActual('../../../contexts/DashboardContext'),
//   useDashBoardState: jest.fn().mockReturnValue({
//     deviceInfo: {
//       list: receivedList,
//       success: false,
//       error: false,
//       failure: false,
//       pending: false,
//       selected: null,
//     },
//   }),
//   useDashBoardDispatch: jest.fn(),
// }));

// jest.mock('react', () => ({
//   ...jest.requireActual('react'),
//   useEffect: (f: () => void) => f(),
// }));

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
        list: [],
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
    mocked_useDashBoardDispatch.mockImplementation(() => jest.fn());

    const component = shallow(<StatusTable />);
  });

  it('fail to status data ', () => {
    const mocked_useDashBoardState = jest.spyOn(
      DashboardContext,
      'useDashBoardState',
    );
    mocked_useDashBoardState.mockImplementation(() => ({
      deviceInfo: {
        list: [],
        success: false,
        error: [],
        failure: true,
        pending: false,
        selected: null,
      },
    }));
    const mocked_useDashBoardDispatch = jest.spyOn(
      DashboardContext,
      'useDashBoardDispatch',
    );
    mocked_useDashBoardDispatch.mockImplementation(() => jest.fn());

    const component = shallow(<StatusTable />);
  });

  it('success to status data ', () => {
    const mocked_useDashBoardState = jest.spyOn(
      DashboardContext,
      'useDashBoardState',
    );
    mocked_useDashBoardState.mockImplementation(() => ({
      deviceInfo: {
        list: receivedList,
        success: true,
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
    mocked_useDashBoardDispatch.mockImplementation(() => jest.fn());

    const component = shallow(<StatusTable />);
  });

  it('pendig to status data ', () => {
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
        pending: true,
        selected: null,
      },
    }));
    const mocked_useDashBoardDispatch = jest.spyOn(
      DashboardContext,
      'useDashBoardDispatch',
    );
    mocked_useDashBoardDispatch.mockImplementation(() => jest.fn());

    const component = shallow(<StatusTable />);
  });

  it('onRefresh, renderStatus, renderDockerRestart, renderOsRestart func ', async () => {
    const mocked_useDashBoardState = jest.spyOn(
      DashboardContext,
      'useDashBoardState',
    );
    mocked_useDashBoardState.mockImplementation(() => ({
      deviceInfo: {
        list: receivedList,
        success: true,
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
    mocked_useDashBoardDispatch.mockImplementation(() => jest.fn());

    const mocked_loadDeviceList = jest.spyOn(
      DashboardContext,
      'loadDeviceList',
    );
    mocked_loadDeviceList.mockImplementation(() => Promise.resolve());

    const component = shallow(<StatusTable />);

    // onRefresh
    await component.find('Button').getElement().props.onClick();

    //renderStatus
    component
      .find('Column')
      .at(2)
      .getElement()
      .props.render(['Database (Up 2 hours)', 'Rapid-Collector (Up 2 hours)']);

    //renderDockerRestart
    const renderDockerRestart = component
      .find('Column')
      .at(4)
      .getElement()
      .props.render('ESP');
    const componenetDockerRestart = shallow(renderDockerRestart);
    componenetDockerRestart.find('AntdIcon').getElement().props.onClick();

    //renderOsRestart
    const renderOsRestart = component
      .find('Column')
      .at(5)
      .getElement()
      .props.render('ESP');
    const componenetOsRestart = shallow(renderOsRestart);
    componenetOsRestart.find('AntdIcon').getElement().props.onClick();
  });

  it('StatusColor func ', () => {
    const up = shallow(<StatusColor status={'Up'} />);
    const exited = shallow(<StatusColor status={'Exited'} />);
    const unknown = shallow(<StatusColor status={'Unknown'} />);
  });
});
