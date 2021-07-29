import { shallow } from 'enzyme';
import React from 'react';
import LogButtion from '../../../../components/Dashboard/System/LogDownload/LogButton';

const fileList = [
  { key: 0, fileName: 'tomcat.log', fileType: 'tomcat', fileSize: 3 },
  { key: 1, fileName: 'tomcat.log', fileType: 'tomcat', fileSize: 3 },
];

describe('renders the component', () => {
  beforeAll(async done => {
    done();
  });

  it('renders correctly', () => {
    const component = shallow(
      <LogButtion
        selected={'ESP'}
        fileList={fileList}
        selectedRowKeys={[0]}
        onRefresh={jest.fn}
      />,
    );

    component.find('Button').at(0).getElement().props.onClick(jest.fn());
    component.find('Button').at(1).getElement().props.onClick(jest.fn());
  });

  it('select is null', () => {
    const component = shallow(
      <LogButtion
        selected={null}
        fileList={fileList}
        selectedRowKeys={[0]}
        onRefresh={jest.fn}
      />,
    );

    component.find('Button').at(0).getElement().props.onClick(jest.fn());
    component.find('Button').at(1).getElement().props.onClick(jest.fn());
  });
});
