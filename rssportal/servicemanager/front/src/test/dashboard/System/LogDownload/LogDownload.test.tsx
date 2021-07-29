import { shallow } from 'enzyme';
import React from 'react';
import LogDownload from '../../../../components/Dashboard/System/LogDownload/LogDownload';

describe('renders the component', () => {
  beforeAll(async done => {
    done();
  });

  it('renders correctly', () => {
    const component = shallow(<LogDownload />);
  });
});
