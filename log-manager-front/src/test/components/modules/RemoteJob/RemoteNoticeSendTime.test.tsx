import { shallow } from 'enzyme';
import React from 'react';
import RemoteNoticeSendTime, {
  RemoteNoticeSendTimeProps,
} from '../../../../components/modules/RemoteJob/RemoteNoticeSendTime';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: RemoteNoticeSendTimeProps = {};
    const component = shallow(<RemoteNoticeSendTime {...input} />);
  });
});
