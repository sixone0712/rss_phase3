import { shallow } from 'enzyme';
import React from 'react';
import RemoteNotice, { RemoteNoticeProps } from '../../../../components/modules/RemoteJob/RemoteNotice';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: RemoteNoticeProps = {};
    const component = shallow(<RemoteNotice {...input} />);
  });
});
