import { shallow } from 'enzyme';
import React from 'react';
import RemoteNoticeEmail, { RemoteNoticeEmailProps } from '../../../../components/modules/RemoteJob/RemoteNoticeEmail';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: RemoteNoticeEmailProps = {
      title: 'Error Summary',
      email: {
        before: 1,
        body: 'test',
        enable: true,
        recipients: ['chpark@canon.bs.co.kr'],
        subject: 'test',
      },
      setEmail: () => {},
    };
    const component = shallow(<RemoteNoticeEmail {...input} />);
  });
});
