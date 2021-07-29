export const vftpConvertDBCommand = (cmdList) => {
    return cmdList.map(item => {
        if (item.cmd_type == "vftp_compat") {
            item.cmd_name = item.cmd_name.includes("%s-%s-")
              ? item.cmd_name.replace("%s-%s-", "")
              : item.cmd_name.replace("%s-%s", "");
        } else {
            item.cmd_name = item.cmd_name.replace("-%s-%s", "");
        }
        return item;
    });
}
