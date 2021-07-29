import { css } from '@emotion/react';
import React from 'react';

export type ConvertRulesProps = {
  children?: React.ReactNode;
};

export default function ConvertRules({ children }: ConvertRulesProps): JSX.Element {
  return (
    <div css={style}>
      <div>Convert Rules</div>
    </div>
  );
}

const style = css``;
