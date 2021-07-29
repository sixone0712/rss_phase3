import { shallow } from 'enzyme';
import React from 'react';
import LocalStatusTable from '../../../../components/modules/LocalStatusTable';
import { LocalStatusTableProps } from '../../../../components/modules/LocalStatusTable/LocalStatusTable';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: LocalStatusTableProps = {};
    const component = shallow(<LocalStatusTable {...input} />);
  });
});
