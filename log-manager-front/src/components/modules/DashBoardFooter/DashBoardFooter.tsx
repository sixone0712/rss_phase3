import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { css } from '@emotion/react';
import { getLogMonitorOos, getLogMonitorServerVersion } from '../../../lib/api/axios/requests';
import { Divider, Drawer } from 'antd';
import ReactMarkdown from 'react-markdown';
import { blue } from '@ant-design/colors';
import gfm from 'remark-gfm';
import { AuditOutlined } from '@ant-design/icons';

export type DashBoardFooterProps = {};

export default function DashBoardFooter({}: DashBoardFooterProps): JSX.Element {
  const [version, setVersion] = useState('-');
  const [about, setAbout] = useState(false);
  const [oos, setOos] = useState('');

  useEffect(() => {
    const getVersion = async () => {
      try {
        const { version } = await getLogMonitorServerVersion();
        return version;
      } catch (e) {
        console.error(e.message);
        console.error(e.response);
        return '-';
      }
    };

    const getOos = async () => {
      try {
        const data = await getLogMonitorOos();
        return data;
      } catch (e) {
        console.error(e.message);
        console.error(e.response);
        return '';
      }
    };

    getVersion().then((value: string) => setVersion(value));
    getOos().then((value: string) => setOos(value));
  }, []);

  const onClose = useCallback(() => {
    setAbout(false);
  }, []);

  const onOpen = useCallback(() => {
    setAbout(true);
  }, []);

  const DrawerTitle = useMemo(
    () => (
      <div>
        <AuditOutlined />
        <span
          css={css`
            margin-left: 0.5rem;
          `}
        >
          About Log Monitor
        </span>
      </div>
    ),
    []
  );

  return (
    <div css={style}>
      <div className="about" onClick={onOpen}>{`About Log Monitor ${version}`}</div>
      <div>Copyright (c) 2021 CANON Inc. All rights reserved.</div>
      <Drawer title={DrawerTitle} placement="left" onClose={onClose} visible={about} width="70rem">
        <div css={customItemStyle}>
          <p>{`Version : ${version}`}</p>
        </div>
        <div css={customItemStyle}>
          <p>Copyright : Copyright (c) 2021 CANON Inc. All rights reserved.</p>
        </div>
        <Divider />
        <ReactMarkdown remarkPlugins={[gfm]} linkTarget="_blank" css={markdownStyle}>
          {oos}
        </ReactMarkdown>
      </Drawer>
    </div>
  );
}

const style = css`
  display: flex;
  justify-content: space-between;
  width: 90rem;

  .about {
    text-decoration: underline;
    cursor: pointer;
    &:hover {
      color: ${blue[4]};
    }
    &:active {
      color: ${blue[6]};
    }
  }
`;

const customItemStyle = css`
  display: flex;
  flex-direction: row;
  margin-bottom: 7px;
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
  line-height: 1.5715;

  p {
    display: inline-block;
    margin-right: 8px;
    color: rgba(0, 0, 0, 0.85);
  }
`;

const markdownStyle = css`
  table {
    width: 100%;
    border: 1px solid #444444;
    border-collapse: collapse;
  }
  th,
  td {
    border: 1px solid #444444;
    padding-left: 0.5rem;
  }
`;
