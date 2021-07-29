import { shallow } from 'enzyme';
import React from 'react';
import RemotePlansTable, { RemotePlansTableProps } from '../../../../components/modules/RemoteJob/RemotePlansTable';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: RemotePlansTableProps = {};
    const component = shallow(<RemotePlansTable {...input} />);
  });
});
