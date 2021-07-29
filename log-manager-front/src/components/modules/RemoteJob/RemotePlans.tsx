import { DesktopOutlined, ProfileOutlined, ReloadOutlined } from '@ant-design/icons';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Button, Row, Select, Space } from 'antd';
import React from 'react';
import useRemoteJobSlices from '../../../hooks/useRemoteJobSlices';
import useSiteName from '../../../hooks/useSiteName';
import { RemoteJobType } from '../../../pages/Status/Remote/Remote';
import RemotePlansTable from './RemotePlansTable';

export type RemotePlansProps = {
  type: RemoteJobType;
};
export default function RemotePlans({ type }: RemotePlansProps): JSX.Element {
  const { selectSite, setSelectSite } = useRemoteJobSlices();
  const { data, disabledSelectSite, refreshSiteName, isFetching } = useSiteName(type, true);

  return (
    <>
      <SelectSiteName align="top">
        <Space css={spaceStyle}>
          <DesktopOutlined />
          <span>User-Fab Name</span>
        </Space>
        <Select
          showSearch
          labelInValue
          css={selectStyle}
          value={selectSite}
          placeholder="Select a site"
          onSelect={setSelectSite}
          loading={isFetching}
          optionFilterProp="children"
          filterOption={(input, option) => option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          disabled={disabledSelectSite}
        >
          {data?.map((item) => (
            <Select.Option key={item.siteId} value={item.siteId} label={item.crasCompanyFabName}>
              {item.crasCompanyFabName}
            </Select.Option>
          ))}
        </Select>
        {type === 'add' && (
          <Button
            type="primary"
            icon={<ReloadOutlined />}
            css={btnStyle}
            onClick={refreshSiteName}
            loading={isFetching}
            disabled={isFetching}
          />
        )}
      </SelectSiteName>
      <SelectPlans>
        <Space css={spaceStyle}>
          <ProfileOutlined />
          <span>Plans</span>
        </Space>
        <RemotePlansTable />
      </SelectPlans>
    </>
  );
}

const SelectSiteName = styled(Row)`
  font-size: 1rem;
  flex-wrap: nowrap;
  /* height: 14.0625rem; */
`;
const SelectPlans = styled(Row)`
  font-size: 1rem;
  margin-top: 2rem;
  flex-wrap: nowrap;
  /* height: 14.0625rem; */
  flex-direction: column;
`;

const spaceStyle = css`
  width: 13.25rem;
  /* font-size: 1.25rem; */
  margin-bottom: 0.5rem;
`;

const selectStyle = css`
  width: 33.75rem;
  text-align: center;
  font-size: inherit;
`;

const btnStyle = css`
  border-radius: 0.625rem;
  margin-left: 0.5rem;
`;
