import { css } from '@emotion/react';
import { Steps } from 'antd';
import React, { useCallback } from 'react';

export type SideStepsProps = {
  current: number;
  stepList: string[];
};

function SideSteps({ current, stepList }: SideStepsProps): JSX.Element {
  const getDescription = useCallback(
    (step: number) => {
      if (current === step) {
        return 'Processing';
      } else if (current < step) {
        return 'Waiting';
      } else if (current > step) {
        return 'Finished';
      }
    },
    [current]
  );

  return (
    <Steps
      current={current}
      direction="vertical"
      css={css`
        width: 16.875rem;
        height: 28.125rem;
        flex-wrap: nowrap;
        border-right: 1px solid #d9d9d9;
        padding-top: 4.125rem;
      `}
    >
      {stepList && stepList.map((item, idx) => <Steps.Step key={idx} title={item} description={getDescription(idx)} />)}
    </Steps>
  );
}

export default React.memo(SideSteps);

const style = css``;
