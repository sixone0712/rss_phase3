import { css } from '@emotion/react';
import { Form, InputNumber } from 'antd';
import React from 'react';

export type FormNumberProps = {
  label: string;
  name: string;
  placeholder?: string;
  disabled: boolean;
  onClick?: () => void;
  min?: number;
  max?: number;
};

export default function FormNumber({ label, name, placeholder, disabled, min, max }: FormNumberProps): JSX.Element {
  return (
    <Form.Item
      label={label}
      name={name}
      rules={[
        {
          required: true,
          message: `Please input ${label.toLocaleLowerCase()}!`,
        },
        min && max
          ? {
              message: `The port must be greater than ${min} and less than ${max}!`,
              type: 'number',
              validator: (rule, value) =>
                new Promise((resolve, reject) => {
                  if (!value) return resolve(true);
                  return +value >= min && +value <= max ? resolve(true) : reject(false);
                }),
            }
          : {
              validator: (rule, value) => new Promise((resolve) => resolve(true)),
            },
      ]}
    >
      <InputNumber
        placeholder={placeholder}
        disabled={disabled}
        min={min}
        max={max}
        // formatter={(value) => {
        //   return value ? JSON.stringify(Math.floor(value)) : '';
        // }}
        css={css`
          width: 100%;
        `}
      />
    </Form.Item>
  );
}
