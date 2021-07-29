import { BookOutlined, MailOutlined } from '@ant-design/icons';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Button, Col, Collapse, Empty, Input, InputNumber, Modal, Row, Select, Space, Spin } from 'antd';
import Checkbox, { CheckboxChangeEvent } from 'antd/lib/checkbox/Checkbox';
import { LabeledValue } from 'antd/lib/select';
import { AxiosError } from 'axios';
import React, { ChangeEvent, KeyboardEventHandler, useCallback, useMemo, useState } from 'react';
import { useMutation } from 'react-query';
import { useDebouncedCallback } from 'use-debounce';
import { v4 as uuidv4 } from 'uuid';
import { searchAddressEmailAndGroup } from '../../../lib/api/axios/requests';
import { AddressInfo } from '../../../lib/api/axios/types';
import { MUTATION_KEY } from '../../../lib/api/query/mutationKey';
import { EMAIL_BEFORE_MAX, EMAIL_SUBJECT_MAX } from '../../../lib/constants';
import { openNotification } from '../../../lib/util/notification';
import { EmailOptionState, EmailOptionStateKey } from '../../../reducers/slices/remoteJob';
import RemoteNoticeEmailAddrBook from './RemoteNoticeEmailAddrBook';

export type RemoteNoticeEmailProps = {
  title: string;
  email: EmailOptionState;
  setEmail: (value: Partial<EmailOptionState>) => void;
};

export default function RemoteNoticeEmail({ title, email, setEmail }: RemoteNoticeEmailProps): JSX.Element {
  const { enable, before, selectedTags, showAddrBook } = email;
  const [subject, setSubject] = useState(email.subject);
  const [body, setBody] = useState(email.body);
  const [active, setActive] = useState(email.enable);
  const debouncedhandleSearch = useDebouncedCallback((value: string) => mutateSearching(value), 400);
  const [keyword, setKeyword] = useState<string>('');
  const [searchResult, setSearchResult] = useState<AddressInfo[]>([]);

  const debounceTextInput = useDebouncedCallback(
    (key: EmailOptionStateKey, value: boolean | string | string[]) => {
      setEmail({ [key]: value });
    },
    // delay in ms
    100
  );

  const onChangeEmail = (key: EmailOptionStateKey, value: any) => {
    if (key === 'subject') {
      setSubject(value as string);
      debounceTextInput(key, value as string);
    } else if (key === 'body') {
      setBody(value as string);
      debounceTextInput(key, value as string);
    } else {
      setEmail({ [key]: value });
    }
  };

  const onChangeEnable = (e: CheckboxChangeEvent) => {
    setActive(e.target.checked);
    onChangeEmail('enable', e.target.checked);
  };

  const onChangeSubject = (e: ChangeEvent<HTMLInputElement>) => onChangeEmail('subject', e.target.value);

  const onChangeContents = (e: ChangeEvent<HTMLTextAreaElement>) => onChangeEmail('body', e.target.value);

  const onChangeBefore = (value: number) => onChangeEmail('before', value);

  const header = useMemo(
    () => (
      <div onClick={() => setActive(!active)}>
        <Space>
          <MailOutlined />
          <div>{title}</div>
        </Space>
      </div>
    ),
    [active]
  );

  const openWarningModal = useCallback((msg: string) => {
    const warning = Modal.warning({
      title: 'Error',
      content: msg,
    });
  }, []);

  const { mutate: mutateSearching, isLoading: isSearching } = useMutation(
    (reqData: string) => searchAddressEmailAndGroup(reqData),
    {
      mutationKey: MUTATION_KEY.ADDRESS_SEARCH_EMAIL_GROUP,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to search email address`, error);
      },
      onSuccess: (result: AddressInfo[]) => {
        setSearchResult(result);
      },
    }
  );

  const handleSearch = async (value: string) => {
    setKeyword(value);
    if (value) {
      debouncedhandleSearch(value);
    } else {
      setSearchResult([]);
    }
  };

  const handleChange = useCallback(
    (selectedItems: LabeledValue[]) => {
      setEmail({ selectedTags: selectedItems });
      setKeyword('');
    },
    [setEmail]
  );

  const handleInputKeyDown: KeyboardEventHandler<HTMLInputElement> = (event) => {
    if (event.key === 'Enter' && !isSearching) {
      if (searchResult.length === 0) {
        const { selectedTags } = email;
        setKeyword('');
        setEmail({ selectedTags: [...selectedTags, { key: uuidv4(), value: keyword, label: keyword }] });
      }
    }
  };

  const handleBlur = useCallback(() => {
    setKeyword('');
    setSearchResult([]);
  }, []);

  const options = useMemo(
    () =>
      searchResult.map((result) => (
        <Select.Option key={result.id} value={result.group ? `group_${result.id}` : `email_${result.id}`}>
          {result.group ? `@${result.name}` : `${result.name} <${result.email}>`}
        </Select.Option>
      )),
    [searchResult]
  );

  const setVisibleAddrBook = (show: boolean) => {
    setEmail({ showAddrBook: show });
  };

  return (
    <EmailSetting>
      <Space align="start">
        <CheckBoxSection>
          <Checkbox checked={enable} onChange={onChangeEnable} />
        </CheckBoxSection>
        <Collapse
          css={collapseStyle(enable)}
          collapsible={enable ? 'header' : 'disabled'}
          activeKey={active ? title : ''}
        >
          <Collapse.Panel header={header} key={title}>
            <RecipientSection>
              <Title>Recipients :</Title>
              <InputValue>
                <Select
                  mode="multiple"
                  placeholder="Input recipients"
                  value={selectedTags}
                  onChange={handleChange}
                  searchValue={keyword}
                  autoClearSearchValue
                  onSearch={handleSearch}
                  style={{ width: '100%' }}
                  optionFilterProp="children"
                  labelInValue
                  onInputKeyDown={handleInputKeyDown}
                  onBlur={handleBlur}
                  notFoundContent={
                    isSearching ? <Spin size="small" /> : keyword ? <Empty description="No Group or Email" /> : null
                  }
                  allowClear
                >
                  {options}
                </Select>
                <Button
                  type="primary"
                  shape="circle"
                  icon={<BookOutlined />}
                  css={addrBookBtnStyle}
                  onClick={() => setVisibleAddrBook(true)}
                />
              </InputValue>
            </RecipientSection>
            <SubjectSection>
              <Title>Subject :</Title>
              <InputValue>
                <Input
                  value={subject}
                  onChange={onChangeSubject}
                  placeholder="Input a subject."
                  allowClear
                  maxLength={EMAIL_SUBJECT_MAX}
                />
              </InputValue>
            </SubjectSection>
            {title === 'Error Summary' && (
              <BodySection
                css={css`
                  align-items: flex-start;
                `}
              >
                <Title>Body :</Title>
                <InputValue>
                  <Input.TextArea
                    autoSize={{ minRows: 3, maxRows: 5 }}
                    value={body}
                    onChange={onChangeContents}
                    placeholder="Input a body."
                    allowClear
                  />
                </InputValue>
              </BodySection>
            )}
            <BeforeSection>
              <Title>Before :</Title>
              <InputBefore>
                <InputNumber
                  min={1}
                  max={EMAIL_BEFORE_MAX}
                  value={before}
                  onChange={onChangeBefore}
                  formatter={(value) => {
                    return value ? JSON.stringify(Math.floor(value)) : '';
                  }}
                />
                <InputUnit>Day</InputUnit>
              </InputBefore>
            </BeforeSection>
          </Collapse.Panel>
        </Collapse>
      </Space>
      <RemoteNoticeEmailAddrBook
        type={title as 'Error Summary' | 'Cras Data' | 'MPA Version'}
        visible={showAddrBook}
        selectedTags={selectedTags}
        setEmail={setEmail}
      />
    </EmailSetting>
  );
}

const EmailSetting = styled(Row)`
  font-size: 1rem;
  flex-wrap: nowrap;
  margin-top: 2rem;
`;

const CheckBoxSection = styled(Col)`
  height: 3rem;
  display: flex;
  align-items: center;
`;

const RecipientSection = styled(Row)`
  flex-direction: row;
  align-items: center;
`;

const SubjectSection = styled(Row)`
  flex-direction: row;
  align-items: center;
  margin-top: 1rem;
`;

const BodySection = styled(Row)`
  flex-direction: row;
  align-items: center;
  margin-top: 1rem;
`;

const BeforeSection = styled(Row)`
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 1rem;
`;

const Title = styled(Col)`
  width: 5rem;
  text-align: right;
  /* margin-top: 0.5rem; */
`;
const InputValue = styled(Col)`
  margin-left: 2rem;
  width: 52.25rem;
  display: flex;
  flex-direction: row;
  justify-content: center;
`;

const InputBefore = styled(Row)`
  margin-left: 2rem;
  align-items: center;
`;

const InputUnit = styled(Col)`
  margin-left: 1rem;
`;

const collapseStyle = (enable: boolean) => css`
  width: 61.5rem;
  cursor: ${!enable && 'not-allowed'};
  .ant-collapse-header {
    pointer-events: ${!enable && 'none'};
  }
`;

const addrBookBtnStyle = css`
  margin-left: 1rem;
`;
