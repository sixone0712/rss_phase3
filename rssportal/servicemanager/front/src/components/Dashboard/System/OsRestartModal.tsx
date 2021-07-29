import React, { useState } from 'react';
import { Col, Form, Input, Modal, Row } from 'antd';
import {
  ExclamationCircleOutlined,
  LockOutlined,
  UserOutlined,
} from '@ant-design/icons';
import axios from 'axios';
import * as DEFINE from '../../../define';
import { openNotification } from '../../../api/notification';
import styled from 'styled-components';

const OsStartIcon = styled(ExclamationCircleOutlined)`
  color: #faad14;
  font-size: 22px;
  padding-right: 16px;
`;

const OsStartText = styled(Col)`
  font-size: 16px;
`;

const title: JSX.Element = (
  <Row>
    <Col>
      <OsStartIcon />
    </Col>
    <OsStartText>Os Restart</OsStartText>
  </Row>
);

const OsRestartModal = ({
  visible,
  setVisible,
  targetDevice,
}: {
  visible: boolean;
  setVisible: React.Dispatch<React.SetStateAction<boolean>>;
  targetDevice: string | null;
}): JSX.Element => {
  const [form] = Form.useForm();
  const [id, setId] = useState('');
  const [password, setPassword] = useState('');
  const [confirmLoading, setConfirmLoading] = useState(false);

  const onFinish = async () => {
    try {
      setConfirmLoading(true);
      const response = await axios.post(
        `${DEFINE.URL_OS_RESTRART}?device=${targetDevice}`,
        {
          id: id,
          password: password,
        },
      );
      console.log('response', response);
      openNotification(
        'success',
        'Success',
        `the OS of ${targetDevice} restart was successful. It takes a few minutes for the OS to restart. Refresh the page in a few minutes.`,
      );
    } catch (e) {
      console.error(e);
      console.error(e.response);
      const errorCode = e.response?.data?.errorCode;
      switch (errorCode) {
        case 500201:
          openNotification(
            'error',
            'Error',
            `the OS of ${targetDevice} restart was failed because the entered root user does not have permission to restart the OS.`,
          );
          break;
        case 500202:
          openNotification(
            'error',
            'Error',
            `the OS of ${targetDevice} restart was failed because of ssh connection. Please check root id and password.`,
          );
          break;
        case 500203:
          openNotification(
            'error',
            'Error',
            `the OS of ${targetDevice} restart was failed because of a problem with the network.`,
          );
          break;
        default:
          openNotification(
            'error',
            'Error',
            `the OS of ${targetDevice} restart was failed.`,
          );
          break;
      }
    } finally {
      form.resetFields();
      setId('');
      setPassword('');
      setConfirmLoading(false);
      setVisible(false);
    }
  };

  const onCancel = () => {
    form.resetFields();
    setId('');
    setPassword('');
    setConfirmLoading(false);
    setVisible(false);
  };

  const onKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && id && password) {
      onFinish();
    }
  };

  return (
    <Modal
      visible={visible}
      title={title}
      closable={false}
      maskClosable={false}
      centered={true}
      width={416}
      okType={'primary'}
      confirmLoading={confirmLoading}
      onOk={onFinish}
      okButtonProps={{ disabled: id === '' || password === '' }}
      cancelButtonProps={{ disabled: confirmLoading }}
      onCancel={onCancel}
    >
      <>
        <div>{`To restart the ${targetDevice}'s OS,`}</div>
        <div>{`Please enter the ${targetDevice}'s root account ID and Password.`}</div>
        <br />
        <Form
          form={form}
          name="normal_login"
          className="login-form"
          initialValues={{ remember: true }}
        >
          <Form.Item
            name="id"
            rules={[{ required: true, message: 'Please input root ID!' }]}
          >
            <Input
              prefix={<UserOutlined className="site-form-item-icon" />}
              placeholder="ID"
              value={id}
              onChange={e => setId(e.target.value)}
              onKeyDown={onKeyDown}
              autoComplete="off"
            />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Please input root Password!' }]}
          >
            <Input
              prefix={<LockOutlined className="site-form-item-icon" />}
              type="password"
              placeholder="Password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              onKeyDown={onKeyDown}
              autoComplete="off"
            />
          </Form.Item>
        </Form>
      </>
    </Modal>
  );
};

export default OsRestartModal;
