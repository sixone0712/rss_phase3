import React from 'react';

export type LogFileList = LogFile[];

export enum LogType {
  USER_LOGIN_OUT = 'user',
  USER_CONTROL = 'control',
  DOWNLOAD_INFO = 'download',
  ESP_OTS_PROCESS = 'subsystem',
  ERROR_EXCEPTION = 'exception',
  TOMCAT = 'tomcat',
  ETC = 'etc',
}

export type CancelInfo = {
  downloadId: string | null;
  cancel: boolean;
  isDownloading: boolean;
};

// Type
export type LogFile = {
  key: React.Key;
  fileTypeName?: string;
  fileType: string;
  fileName: string;
  fileSize: number;
};

export const logFilter = [
  {
    text: 'User Login/Logout',
    value: LogType.USER_LOGIN_OUT,
  },
  {
    text: 'User Control',
    value: LogType.USER_CONTROL,
  },
  {
    text: 'Download Infomation',
    value: LogType.DOWNLOAD_INFO,
  },
  {
    text: 'ESP/OTS Process',
    value: LogType.ESP_OTS_PROCESS,
  },
  {
    text: 'Error Exception',
    value: LogType.ERROR_EXCEPTION,
  },
  {
    text: 'Tomcat',
    value: LogType.TOMCAT,
  },
  {
    text: 'etc',
    value: LogType.ETC,
  },
];
