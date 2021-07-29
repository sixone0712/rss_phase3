import { Form, Select } from 'antd';
import { SelectValue } from 'antd/lib/select';
import React from 'react';

export type FormSelectMultiple<T> = {
  label: string;
  name: string;
  options: T[] | undefined;
  ObjkeyName?: string;
  ObjvalueName?: string;
  ObjLabelName?: string;
  disabled?: boolean;
  loading?: boolean;
  mode?: 'multiple' | 'tags' | undefined;
  required?: boolean;
  placeholder?: React.ReactNode;
  onSelect?: (value: SelectValue) => void;
  etc?: any;
};

export default function FormSelectMultiple<T>({
  label,
  name,
  options,
  ObjkeyName,
  ObjvalueName,
  ObjLabelName,
  disabled = false,
  loading = false,
  mode,
  required = true,
  placeholder,
  onSelect,
  etc = undefined,
}: FormSelectMultiple<T>): JSX.Element {
  return (
    <Form.Item
      label={label}
      name={name}
      required={required}
      rules={
        required
          ? [
              {
                required: true,
                message: `Please select ${label.toLocaleLowerCase()}!`,
              },
            ]
          : undefined
      }
    >
      <Select
        mode={mode}
        showArrow
        showSearch={false}
        disabled={disabled}
        loading={loading}
        placeholder={placeholder}
        onSelect={onSelect}
        {...etc}
      >
        {options &&
          options.length > 0 &&
          options?.map((item: any) => (
            <Select.Option key={ObjkeyName ? item[ObjkeyName] : item} value={ObjvalueName ? item[ObjvalueName] : item}>
              {ObjLabelName ? item[ObjLabelName] : item}
            </Select.Option>
          ))}
      </Select>
    </Form.Item>
  );
}
