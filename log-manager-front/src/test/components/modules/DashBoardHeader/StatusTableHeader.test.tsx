import { shallow } from 'enzyme';
import React from 'react';
import StatusTableHeader, {
  StatusTableHeaderProps,
} from '../../../../components/modules/StatusTableHeader/StatusTableHeader';

describe('renders the component', () => {
  it('renders correctly', () => {
    const input: StatusTableHeaderProps = {
      listCount: 1,
      newBtn: false,
      onClickRefresh: () => {},
      refreshBtn: false,
    };
    const component = shallow(<StatusTableHeader {...input} />);
  });
});
