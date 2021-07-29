import { shallow } from 'enzyme';
import React from 'react';
import LocalConfirm, { LocalConfirmProps } from '../../../../components/modules/LocalJob/LocalConfirm';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: LocalConfirmProps = {};
    const component = shallow(<LocalConfirm {...input} />);
  });
});
