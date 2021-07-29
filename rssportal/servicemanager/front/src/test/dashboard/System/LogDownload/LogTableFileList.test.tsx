import { shallow } from 'enzyme';
import React from 'react';
import LogTableFileList from '../../../../components/Dashboard/System/LogDownload/LogTableFileList';

const fileList = [
  { key: 0, fileName: 'tomcat.log', fileType: 'tomcat', fileSize: 3 },
  { key: 1, fileName: 'tomcat.log', fileType: 'tomcat', fileSize: 3 },
];

const receivedList = [
  {
    key: 0,
    type: 'ESP',
    name: 'ESP',
    ip: '',
    status: ['Database (Up 2 hours)', 'Rapid-Collector (Up 2 hours)'],
    volume: '0.1G / 163.0G',
  },
  {
    key: 0,
    type: 'OTS',
    name: 'OTS_DEV',
    ip: '10.1.31.228',
    status: ['FileServiceCollect (Up 2 hours)'],
    volume: '0.1G / 163.0G',
  },
];

const listState = {
  loading: false,
  data: [],
  error: null,
};

describe('renders the component', () => {
  beforeAll(async done => {
    done();
  });

  it('renders correctly', () => {
    const component = shallow(
      <LogTableFileList
        selected={'ESP'}
        fileList={fileList}
        listState={listState}
        onLoadFileList={() => {
          return;
        }}
      />,
    );
    console.log('component', component.debug());

    const component2 = shallow(
      <LogTableFileList
        selected={null}
        fileList={fileList}
        listState={listState}
        onLoadFileList={() => {
          return;
        }}
      />,
    );
  });

  it('rowSelection, onTableChange', () => {
    const component = shallow(
      <LogTableFileList
        selected={'ESP'}
        fileList={fileList}
        listState={listState}
        onLoadFileList={() => {
          return;
        }}
      />,
    );

    component.find('Table').getElement().props.rowKey({ key: 0 });

    component
      .find('Table')
      .getElement()
      .props.rowSelection.onSelectAll('ESP', [], []);

    component
      .find('Table')
      .getElement()
      .props.rowSelection.onSelectAll(null, [], []);

    component
      .find('Table')
      .getElement()
      .props.onChange([], { fileType: 'user' }, [], {
        currentDataSource: ['ddd'],
        action: 'filter',
      });

    component
      .find('Table')
      .getElement()
      .props.rowSelection.onSelectAll('ESP', [], []);

    component
      .find('Table')
      .getElement()
      .props.onChange([], { fileType: null }, [], {
        currentDataSource: [],
        action: 'filter',
      });

    component
      .find('Table')
      .getElement()
      .props.onChange([], { fileType: null }, [], {
        currentDataSource: [],
        action: null,
      });
  });

  it('onRow', () => {
    const component = shallow(
      <LogTableFileList
        selected={'ESP'}
        fileList={fileList}
        listState={listState}
        onLoadFileList={() => {
          return;
        }}
      />,
    );

    component
      .find('Table')
      .getElement()
      .props.onRow(
        {
          key: undefined,
          fileName: 'tomcat.log',
          fileType: 'tomcat',
          fileSize: 3,
        },
        0,
      )
      .onClick(null);

    component
      .find('Table')
      .getElement()
      .props.onRow(
        { key: 0, fileName: 'tomcat.log', fileType: 'tomcat', fileSize: 3 },
        0,
      )
      .onClick(null);
  });

  it('fileTypeFilterFunc', () => {
    const component = shallow(
      <LogTableFileList
        selected={'ESP'}
        fileList={fileList}
        listState={listState}
        onLoadFileList={() => {
          return;
        }}
      />,
    );

    component.find('Column').at(0).getElement().props.onFilter();
    component
      .find('Column')
      .at(0)
      .getElement()
      .props.onFilter('user', { fileType: 'user' });
    component.find('Column').at(0).getElement().props.render(0, {
      key: 0,
      fileName: 'tomcat.log',
      fileTypeName: 'tomcat.log',
      fileType: 'tomcat',
      fileSize: 3,
    });
    component.find('Column').at(2).getElement().props.render(0);
  });

  it('fileTypeFilterFunc', () => {
    const component = shallow(
      <LogTableFileList
        selected={'ESP'}
        fileList={fileList}
        listState={listState}
        onLoadFileList={() => {
          return;
        }}
      />,
    );
    component.find('Table').getElement().props.pagination.onChange();
  });
});
