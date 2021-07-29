import { shallow } from 'enzyme';
import React from 'react';
import RemotePlans, { RemotePlansProps } from '../../../../components/modules/RemoteJob/RemotePlans';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: RemotePlansProps = {
      type: 'add',
    };
    const component = shallow(<RemotePlans {...input} />);
  });
});
