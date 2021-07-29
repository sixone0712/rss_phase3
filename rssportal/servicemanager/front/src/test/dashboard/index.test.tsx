import React from 'react';
import { shallow } from 'enzyme';
import DashBoard, { DashboardFooter } from '../../components/Dashboard';

describe('renders the component', () => {
  beforeAll(async done => {
    done();
  });

  it('renders correctly', () => {
    const component = shallow(<DashBoard />);
    const componentFooter = shallow(<DashboardFooter />);
  });
});
