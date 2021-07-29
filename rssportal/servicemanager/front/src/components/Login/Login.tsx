import React, { useEffect } from 'react';
import { Form, Input, Button, Checkbox, Row, Col, Layout, Divider } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import axios from 'axios';
import * as DEFINE from '../../define';
import { useHistory } from 'react-router-dom';
import { openNotification } from '../../api/notification';
import md5 from 'md5';
import { useUserDispatch } from '../../contexts/UserContext';
import { userInfoSetUser } from '../../redux/userinfo/actions';
import { useDispatch } from 'react-redux';

function Login() {
  const history = useHistory();
  const [form] = Form.useForm();
  //const dispatch = useUserDispatch();
  const dispatchRedux = useDispatch();

  useEffect(() => {
    const unauthorizedError = sessionStorage.getItem('unauthorizedError');

    console.log('unauthorizedError', unauthorizedError);
    if (unauthorizedError) {
      sessionStorage.clear();
      openNotification(
        'info',
        'Logged Out',
        `Your login has expired and has been redirected to the login page. Please log in again.`,
      );
    }
  }, []);

  const onFinish = async ({
    username,
    password,
  }: {
    username: string;
    password: string;
  }) => {
    form.resetFields();
    try {
      const response: any = await axios.get(
        `${DEFINE.URL_LOGIN}?username=${username}&password=${md5(password)}`,
      );
      console.log('response', response);
      if (response.status === 200) {
        const { username, permission } = response.data;
        //dispatch({ type: 'SET_USER_INFO', data: { username, permission } });
        dispatchRedux(userInfoSetUser({ username, permission }));
        history.push(DEFINE.URL_PAGE_DASHBOARD_SYSTEM);
      }
    } catch (e) {
      console.log(e);
      console.log('e.response', e.response);
      if (e?.response?.status === 400)
        openNotification(
          'error',
          'Error',
          'Login failed for a invalid username or password.',
        );
      else openNotification('error', 'Error', 'A network error has occurred.');
    }
  };

  return (
    <Layout
      style={{
        height: '100vh',
        backgroundColor: '#485461',
        backgroundImage: 'linear-gradient(315deg, #485461 0%, #28313b 74%)',
      }}
    >
      <Row justify="center" align="middle" style={{ height: '100vh' }}>
        <Col>
          <Form
            form={form}
            name="normal_login"
            className="login-form"
            initialValues={{ remember: true }}
            onFinish={onFinish}
            style={{
              width: '400px',
              maxWidth: '400px',
              height: '350px',
              minHeight: '350px',
              paddingTop: '50px',
              paddingLeft: '20px',
              paddingRight: '20px',
              backgroundColor: '#DFE5ED',
              // backgroundColor: 'white',
              borderRadius: '0.25rem',
              boxShadow: '0 0.5rem 1rem rgba(0,0,0,0.15)',
            }}
          >
            <Form.Item>
              <div
                style={{
                  fontSize: '1.4rem',
                  fontWeight: 600,
                  textAlign: 'center',
                }}
              >
                Log-in to your account
              </div>
            </Form.Item>
            <Divider />
            <Form.Item
              name="username"
              rules={[
                {
                  required: true,
                  message: 'Please input your username!',
                },
              ]}
            >
              <Input
                size="large"
                prefix={<UserOutlined className="site-form-item-icon" />}
                placeholder="Username"
                autoComplete="off"
                style={{
                  borderRadius: '0.25rem',
                  boxShadow: '0 0.5rem 1rem rgba(0,0,0,0.15)',
                }}
              />
            </Form.Item>
            <Form.Item
              name="password"
              rules={[
                {
                  required: true,
                  message: 'Please input your password!',
                },
              ]}
            >
              <Input
                size="large"
                prefix={<LockOutlined className="site-form-item-icon" />}
                type="password"
                placeholder="Password"
                autoComplete="off"
                style={{
                  borderRadius: '0.25rem',
                  boxShadow: '0 0.5rem 1rem rgba(0,0,0,0.15)',
                }}
              />
            </Form.Item>
            <Form.Item>
              <Button
                size="large"
                type="primary"
                htmlType="submit"
                className="login-form-button"
                style={{
                  marginTop: '10px',
                  width: '100%',
                  borderRadius: '0.25rem',
                  // backgroundColor: 'black',
                  boxShadow: '0 0.5rem 1rem rgba(0,0,0,0.15)',
                }}
              >
                Sign in
              </Button>
            </Form.Item>
          </Form>
        </Col>
      </Row>
    </Layout>
  );
}

export default Login;
