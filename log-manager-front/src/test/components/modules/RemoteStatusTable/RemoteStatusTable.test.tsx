import { shallow } from 'enzyme';
import React from 'react';
import RemoteStatusTable from '../../../../components/modules/RemoteStatusTable';
import { RemoteStatusTableProps } from '../../../../components/modules/RemoteStatusTable/RemoteStatusTable';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: RemoteStatusTableProps = {};
    const component = shallow(<RemoteStatusTable {...input} />);
  });
});
