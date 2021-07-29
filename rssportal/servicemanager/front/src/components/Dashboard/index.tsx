import React from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import DashboardHeader from './Header/DashboardHeader';
import SystemInfo from './System';
import { Col, Layout, Row } from 'antd';
import * as DEFINE from '../../define';

const { Footer } = Layout;

export const DashboardFooter = (): JSX.Element => (
  <Footer className="dashboard-footer">
    <Row className="text" justify={'end'} align={'middle'}>
      <Col>Copyright CANON INC. 2020</Col>
    </Row>
  </Footer>
);

function Dashboard(): JSX.Element {
  return (
    <>
      <Layout className="dashboard">
        <DashboardHeader />
        <Switch>
          <Route
            path={DEFINE.URL_PAGE_DASHBOARD_SYSTEM}
            component={SystemInfo}
          />
          <Redirect path="*" to={DEFINE.URL_PAGE_NOT_FOUND} />
        </Switch>
        <DashboardFooter />
      </Layout>
    </>
  );
}

export default Dashboard;
