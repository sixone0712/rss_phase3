import { shallow } from 'enzyme';
import React from 'react';
import LocalConfigure, { LocalConfigureProps } from '../../../../components/modules/LocalJob/LocalConfigure';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: LocalConfigureProps = {};
    const component = shallow(<LocalConfigure {...input} />);
  });
});
