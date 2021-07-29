import React from 'react';
import { shallow } from 'enzyme';
import Login from '../../../components/Login/Login';
import axios from 'axios';
import md5 from 'md5';

// jest.mock('react', () => ({
//   ...jest.requireActual('react'),
//   useEffect: (f: () => void) => f(),
// }));

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

jest.mock('md5');
const mockedMd5 = md5 as jest.Mock;

describe('renders the component', () => {
  beforeAll(async done => {
    mockedAxios.get.mockRestore();
    mockedAxios.post.mockRestore();
    mockedAxios.delete.mockRestore();
    mockedAxios.put.mockRestore();
    mockedAxios.patch.mockRestore();
    mockedMd5.mockRestore();
    sessionStorage.clear();
    done();
  });

  it('renders correctly without unauthorizedError', () => {
    sessionStorage.setItem('unauthorizedError', 'true');
    const component = shallow(<Login />);
    //expect(component).toMatchSnapshot();
  });

  it('renders correctly with unauthorizedError', async () => {
    const component = shallow(<Login />);
  });

  it('onFinish function : success ', async () => {
    sessionStorage.setItem('unauthorizedError', 'true');
    mockedAxios.get.mockResolvedValueOnce({
      status: 200,
    });
    mockedMd5.mockReturnValueOnce('hashedpassword');
    const component = shallow(<Login />);
    await component
      .find('ForwardRef(InternalForm)')
      .getElement()
      .props.onFinish({ password: 'password' });
  });

  it('onFinish function : fail ', async () => {
    mockedMd5.mockReturnValue('hashedpassword');
    sessionStorage.setItem('unauthorizedError', 'true');
    mockedAxios.get.mockRejectedValueOnce({
      response: {
        status: 400,
      },
    });
    const component = shallow(<Login />);
    await component
      .find('ForwardRef(InternalForm)')
      .getElement()
      .props.onFinish({ password: 'password' });

    mockedAxios.get.mockRejectedValueOnce({
      response: {
        status: 400,
      },
    });
    await component
      .find('ForwardRef(InternalForm)')
      .getElement()
      .props.onFinish('password');
  });
});
