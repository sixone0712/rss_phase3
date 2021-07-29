import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Button, Col, Space } from 'antd';
import React, { useCallback } from 'react';
import { RemoteJobType } from '../../../pages/Status/Remote/Remote';

export type StopButtonProps = {
  current: number;
  setCurrent: React.Dispatch<React.SetStateAction<number>>;
  lastStep: number;
  nextAction: () => boolean;
  type?: RemoteJobType;
};

const Container = styled(Col)`
  /* display: flex;
  justify-content: space-between;
  width: 47.125rem;
  margin-left: 29.375rem;
  margin-right: 1.75rem; */
`;

export default function StopButton({
  current,
  setCurrent,
  lastStep,
  nextAction,
  type = 'add',
}: StopButtonProps): JSX.Element {
  const onNext = useCallback(() => {
    if (current <= lastStep && nextAction()) {
      if (current !== lastStep) setCurrent((prevState) => prevState + 1);
    }
  }, [current, setCurrent, nextAction]);

  const onPrev = useCallback(() => {
    if (current > 0) setCurrent((prevState) => prevState - 1);
  }, [current, setCurrent]);

  return (
    <Container>
      <Space>
        {current > 0 && (
          <Button type="primary" css={btnStyle} onClick={onPrev}>
            Prev
          </Button>
        )}
        {current < lastStep && (
          <Button type="primary" css={btnStyle} onClick={onNext}>
            Next
          </Button>
        )}
        {current >= lastStep && (
          <Button type="primary" css={btnStyle} onClick={onNext}>
            {type === 'add' ? 'Add' : 'Edit'}
          </Button>
        )}
      </Space>
    </Container>
  );
}

const btnStyle = css`
  border-radius: 0.625rem;
  width: 4rem;
`;
