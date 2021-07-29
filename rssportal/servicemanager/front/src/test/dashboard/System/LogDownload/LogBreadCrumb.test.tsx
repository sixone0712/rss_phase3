import { shallow } from 'enzyme';
import React from 'react';
import LogBreadCrumb from '../../../../components/Dashboard/System/LogDownload/LogBreadCrumb';

describe('renders the component', () => {
  beforeAll(async done => {
    done();
  });

  it('renders correctly', () => {
    const component = shallow(<LogBreadCrumb selected={'ESP'} />);
    console.log('component', component.debug());
  });
});
