import React from 'react';
import { shallow } from 'enzyme';
import DashboardHeader, {
  menu as Menu,
} from '../../../components/Dashboard/Header/DashboardHeader';
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
    const component = shallow(<DashboardHeader />);
  });

  it('overlay function', async () => {
    const component = shallow(<DashboardHeader />);
    component.find('Dropdown').getElement().props.overlay();
    const mockedE = {
      preventDefault: jest.fn(),
    };
    component.find('a').getElement().props.onClick(mockedE);
  });

  it('menu function', async () => {
    const mockedHistory = {
      push: jest.fn(),
    };
    const component = shallow(<Menu history={mockedHistory} />);
    mockedAxios.get.mockResolvedValueOnce({
      status: 200,
    });
    await component.dive().dive().find('MenuItem').getElement().props.onClick();

    mockedAxios.get.mockRejectedValueOnce({
      response: {
        status: 500,
      },
    });
    await component.dive().dive().find('MenuItem').getElement().props.onClick();
  });
});
