import { css } from '@emotion/react';
import React from 'react';
import { Route, Switch } from 'react-router-dom';
import Local from './Local';
import Remote from './Remote';
import History from './History';
import { PAGE_URL } from '../../lib/constants';

export type StatusProps = {};

export default function Status({}: StatusProps) {
  return (
    <Switch>
      <Route path={PAGE_URL.STATUS_LOCAL_ROUTE} exact component={Local} />
      <Route path={PAGE_URL.STATUS_LOCAL_ADD_ROUTE} exact component={Local.AddJob} />
      <Route path={PAGE_URL.STATUS_REMOTE_ROUTE} exact component={Remote} />
      <Route path={PAGE_URL.STATUS_REMOTE_ADD_ROUTE} exact component={Remote.AddJob} />
      <Route path={PAGE_URL.STATUS_REMOTE_EDIT_ROUTE} exact component={Remote.EditJob} />
      <Route
        path={[PAGE_URL.STATUS_REMOTE_BUILD_HISTORY_ROUTE, PAGE_URL.STATUS_LOCAL_BUILD_HISTORY_ROUTE]}
        component={History}
      />
    </Switch>
  );
}

const style = css``;
