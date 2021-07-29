import {
  DEVICE_INFO_LIST,
  RES_DEVICE_INFO,
  RES_DEVICE_INFO_CONTAINER,
} from './types';

export function convDeviceInfo(data: RES_DEVICE_INFO[]): DEVICE_INFO_LIST[] {
  return (
    data?.map((item: RES_DEVICE_INFO, index: number) => {
      const status = item.containers?.map(
        (container: RES_DEVICE_INFO_CONTAINER) => {
          return `${container.name} (${container.status?.replace(
            /\(.+\)\s/g,
            '',
          )})`;
        },
      );

      const device: DEVICE_INFO_LIST = {
        key: index,
        name: item.name,
        type: item.type,
        ip: item.host,
        status: status,
        volume:
          item.volumeUsed === 'Unknown' || item.volumeTotal === 'Unknown'
            ? 'Unknown'
            : `${item.volumeUsed} / ${item.volumeTotal}`,
      };

      return device;
    }) || []
  );
}
