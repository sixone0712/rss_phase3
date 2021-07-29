import { shallow } from 'enzyme';
import React from 'react';
import SystemInfo from '../../../components/Dashboard/System';

// jest.mock('react', () => ({
//   ...jest.requireActual('react'),
//   useEffect: (f: () => void) => f(),
// }));

jest.mock('../../../contexts/DashboardContext', () => ({
  ...jest.requireActual('../../../contexts/DashboardContext'),
  loadDeviceList: jest.fn().mockResolvedValue(true),
  useDashBoardDispatch: jest.fn(),
}));

describe('renders the component', () => {
  beforeAll(async done => {
    done();
  });

  it('renders correctly', () => {
    const component = shallow(<SystemInfo />);
    console.log('component', component.debug());
  });
});
