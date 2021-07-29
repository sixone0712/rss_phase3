import { Modal } from 'antd';
import axios from 'axios';
import { openNotification } from './notification';
import * as DEFINE from '../define';

export const execDockerRestart = (device: string, onRefresh: () => void) => {
  const modal = Modal.confirm({
    centered: true,
  });

  modal.update({
    title: 'Docker Restart',
    // icon: <ExclamationCircleOutlined />,
    content: `Do you want to restart the docker of ${device}?`,
    onOk: async () => {
      modal.update({
        cancelButtonProps: { disabled: true },
      });

      try {
        const { data } = await axios.post(
          `${DEFINE.URL_DOCKER_RESTART}?device=${device}`,
        );
        openNotification(
          'success',
          'Success',
          `the docker of ${device} restart was successful.`,
        );
        onRefresh();
      } catch (e) {
        const errorCode = e.response?.data?.errorCode;
        switch (errorCode) {
          case 500101:
            openNotification(
              'error',
              'Error',
              `the docker of ${device} restart was failed because the Docker Container does not exist.`,
            );
            break;
          case 500102:
            openNotification(
              'error',
              'Error',
              `the docker of ${device} restart was failed because the Docker engine failed with a communication.`,
            );
            break;
          case 500103:
            openNotification(
              'error',
              'Error',
              `the docker of ${device} restart was failed because of a problem with the network.`,
            );
            break;
          default:
            openNotification(
              'error',
              'Error',
              `the docker of ${device} restart was failed.`,
            );
            break;
        }
      }
    },
  });
};
