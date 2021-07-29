import { shallow } from 'enzyme';
import React from 'react';
import OsRestartModal from '../../../components/Dashboard/System/OsRestartModal';
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

  // it('renders correctly', () => {
  //   const component = shallow(
  //     <OsRestartModal
  //       visible={true}
  //       setVisible={jest.fn()}
  //       targetDevice={'ESP'}
  //     />,
  //   );
  // });

  it('input id/password', async () => {
    const component = shallow(
      <OsRestartModal
        visible={true}
        setVisible={jest.fn()}
        targetDevice={'ESP'}
      />,
    );

    console.log('component', component.debug());

    // input id
    component
      .find('Input')
      .at(0)
      .getElement()
      .props.onChange({ target: { value: 'root' } });

    //input password
    component
      .find('Input')
      .at(1)
      .getElement()
      .props.onChange({ target: { value: 'password' } });

    // onFinish
    // success
    mockedAxios.post.mockResolvedValueOnce({
      status: 200,
    });
    await component.find('Modal').getElement().props.onOk();

    //fail
    mockedAxios.post.mockRejectedValueOnce({
      response: {
        data: {
          errorCode: 500201,
        },
      },
    });
    await component.find('Modal').getElement().props.onOk();
    mockedAxios.post.mockRejectedValueOnce({
      response: {
        data: {
          errorCode: 500202,
        },
      },
    });
    await component.find('Modal').getElement().props.onOk();
    mockedAxios.post.mockRejectedValueOnce({
      response: {
        data: {
          errorCode: 500203,
        },
      },
    });
    await component.find('Modal').getElement().props.onOk();
    await component.find('Modal').getElement().props.onOk();
    mockedAxios.post.mockRejectedValueOnce({
      response: {
        data: {
          errorCode: 0,
        },
      },
    });
    await component.find('Modal').getElement().props.onOk();

    // cancel
    await component.find('Modal').getElement().props.onCancel();
  });

  it('enter key press', async () => {
    const component = shallow(
      <OsRestartModal
        visible={true}
        setVisible={jest.fn()}
        targetDevice={'ESP'}
      />,
    );
    // input id
    component
      .find('Input')
      .at(0)
      .getElement()
      .props.onChange({ target: { value: 'root' } });

    //input password
    component
      .find('Input')
      .at(1)
      .getElement()
      .props.onChange({ target: { value: 'password' } });

    // enter key press
    await component
      .find('Input')
      .at(0)
      .getElement()
      .props.onKeyDown({ key: 'Enter' });
  });
});
