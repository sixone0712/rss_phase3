import { shallow } from 'enzyme';
import React from 'react';
import LogTable from '../../../../components/Dashboard/System/LogDownload/LogTable';
import * as DashboardContext from '../../../../contexts/DashboardContext';
import * as useAsyncAxios from '../../../../hooks/useAsyncAxios';
import axios from 'axios';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('renders the component', () => {
  beforeAll(async done => {
    mockedAxios.get.mockRestore();
    mockedAxios.post.mockRestore();
    mockedAxios.delete.mockRestore();
    mockedAxios.put.mockRestore();
    mockedAxios.patch.mockRestore();
    done();
  });

  it('renders correctly', () => {
    mockedAxios.get.mockResolvedValueOnce({
      response: {
        status: 200,
      },
    });
    const mocked_useDashBoardState = jest.spyOn(
      DashboardContext,
      'useDashBoardState',
    );
    mocked_useDashBoardState.mockImplementation(() => ({
      deviceInfo: {
        list: [],
        success: false,
        error: false,
        failure: false,
        pending: false,
        selected: 'ESP',
      },
    }));

    const mocked_useAsyncAxios = jest.spyOn(useAsyncAxios, 'default');
    mocked_useAsyncAxios.mockImplementationOnce((): any => {
      console.log('mocked_useAsyncAxios');
      return [
        {
          error: null,
          loading: false,
          data: {
            data: {
              list: [
                { fileName: 'tomcat.log', fileType: 'tomcat', fileSize: 3 },
              ],
            },
          },
        },
        () => Promise.resolve(),
      ];
    });

    const component = shallow(<LogTable />);
    component.find('LogTableFileList').getElement().props.onLoadFileList();
  });

  it('response fileList data is null and error occur', async () => {
    const mocked_useDashBoardState = jest.spyOn(
      DashboardContext,
      'useDashBoardState',
    );
    mocked_useDashBoardState.mockImplementation(() => ({
      deviceInfo: {
        list: [],
        success: false,
        error: false,
        failure: false,
        pending: false,
        selected: null,
      },
    }));

    const mocked_useAsyncAxios = jest.spyOn(useAsyncAxios, 'default');
    mocked_useAsyncAxios.mockImplementationOnce((): any => {
      console.log('mocked_useAsyncAxios');
      return [
        { loading: false, error: true, data: null },
        () => Promise.resolve(),
      ];
    });

    const component = shallow(<LogTable />);
    console.log('component', component.debug());
  });
});
