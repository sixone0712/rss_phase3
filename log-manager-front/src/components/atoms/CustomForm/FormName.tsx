import { Form, Input } from 'antd';
import React from 'react';
import { CONFIGURE_NAME_MAX } from '../../../lib/constants';
import { regExpName } from '../../../lib/util/validation';

interface FormNameProps {
  label: string;
  name: string;
  placeholder?: string;
  disabled?: boolean;
  isMultilingual?: boolean;
  onClick?: () => void;
  required?: boolean;
}

export default function FormName({
  label,
  name,
  placeholder,
  disabled = false,
  isMultilingual = false,
  required = true,
}: FormNameProps): JSX.Element {
  const Tooltip = () => (
    <div>
      <p>Characters that can be entered: alphabet, number, dot(.), low line(_), hyphen(-).</p>
      <p>Start and end must be entered in alphabet or number.</p>
      <p>Allowed to be at least 3 characters long and up to 30 characters long.</p>
    </div>
  );

  const nameRegExp = isMultilingual
    ? {}
    : {
        message: `Please input in the correct format! See tooltip!`,
        pattern: regExpName(),
      };

  return (
    <Form.Item
      label={label}
      name={name}
      rules={[
        {
          required: true,
          message: `Please input ${label.toLocaleLowerCase()}!`,
        },
        nameRegExp,
      ]}
      tooltip={
        !isMultilingual && {
          title: Tooltip,
        }
      }
      required={required}
    >
      <Input placeholder={placeholder} disabled={disabled} maxLength={CONFIGURE_NAME_MAX} />
    </Form.Item>
  );
}
