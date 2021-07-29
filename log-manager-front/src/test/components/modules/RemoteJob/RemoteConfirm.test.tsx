import { shallow } from 'enzyme';
import React from 'react';
import RemoteConfirm, { RemoteConfirmProps } from '../../../../components/modules/RemoteJob/RemoteConfirm';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: RemoteConfirmProps = {};
    const component = shallow(<RemoteConfirm {...input} />);
  });
});
