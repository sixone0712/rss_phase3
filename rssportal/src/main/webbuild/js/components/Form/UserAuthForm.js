import React, {useState} from "react"
import PropTypes from "prop-types";
import * as Define from "../../define"
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons";
import {Toast, ToastBody, ToastHeader, Tooltip} from "reactstrap";

const permissionMsg = (name) => {
    let msg = "";
    switch (name) {
        case Define.PERM_MANUAL_DOWNLOAD_FTP:
            msg = Define.PERM_MSG_MANUAL_DOWNLOAD_FTP;
            break;
        case Define.PERM_MANUAL_DOWNLOAD_VFTP:
            msg = Define.PERM_MSG_MANUAL_DOWNLOAD_VFTP;
            break;
        case Define.PERM_AUTO_COLLECTION_SETTING:
            msg = Define.PERM_MSG_AUTO_COLLECTION_SETTING;
            break;
        case Define.PERM_SYSTEM_LOG_DOWNLOAD:
            msg = Define.PERM_MSG_SYSTEM_LOG_DOWNLOAD;
            break;
        case Define.PERM_SYSTEM_RESTART:
            msg = Define.PERM_MSG_SYSTEM_RESTART;
            break;
        case Define.PERM_ACCOUNT_SETTING:
            msg = Define.PERM_MSG_ACCOUNT_SETTING;
            break;
        case Define.PERM_CONFIG_SETTING:
            msg = Define.PERM_MSG_CONFIG_SETTING;
            break;
        default: break;
    }
    return msg;
}

function UserAuthFrom({ authValue, changeFunc, loginUserAuth }){
    return(
        <>
            <div className="Custom-Permission">
                <NormalUser authValue={authValue} loginUserAuth={loginUserAuth} changeFunc={changeFunc} />
                <AdminUser authValue={authValue} loginUserAuth={loginUserAuth} changeFunc={changeFunc} />
            </div>
        </>
    );
}

UserAuthFrom.propTypes = {
    authValue: PropTypes.object.isRequired,
    changeFunc: PropTypes.func.isRequired,
    loginUserAuth: PropTypes.object.isRequired,
};

export default React.memo(UserAuthFrom);

const NormalUser = React.memo(({ authValue, loginUserAuth, changeFunc }) => {
    return (
        <Toast className={"normal-user"}>
            <ToastHeader>
                Normal User Permission
            </ToastHeader>
            <ToastBody>
                <Permission
                    name={"manual_ftp"}
                    checked={true}
                    changeFunc={() => {
                    }}
                    userAuth={true}
                />
                <Permission
                    name={"manual_vftp"}
                    checked={authValue.manual_vftp}
                    changeFunc={changeFunc}
                    userAuth={loginUserAuth.manual_vftp}
                />
                <Permission
                    name={"auto"}
                    checked={authValue.auto}
                    changeFunc={changeFunc}
                    userAuth={loginUserAuth.auto}
                />
            </ToastBody>
        </Toast>
    );
}, ({ authValue: prev_auth }, { authValue: next_auth }) => (
    prev_auth.manual_vftp === next_auth.manual_vftp && prev_auth.auto === next_auth.auto
));

const AdminUser = React.memo(({ authValue, loginUserAuth, changeFunc }) => {
    return (
        <Toast className={"administrator"}>
            <ToastHeader>
                Administrator Permission
            </ToastHeader>
            <ToastBody>
                <Permission
                    name={"system_log"}
                    checked={authValue.system_log}
                    changeFunc={changeFunc}
                    userAuth={loginUserAuth.system_log}
                />
                <Permission
                    name={"system_restart"}
                    checked={authValue.system_restart}
                    changeFunc={changeFunc}
                    userAuth={loginUserAuth.system_restart}
                />
                <Permission
                    name={"account"}
                    checked={authValue.account}
                    changeFunc={changeFunc}
                    userAuth={loginUserAuth.account}
                />
                <Permission
                    name={"config"}
                    checked={authValue.config}
                    changeFunc={changeFunc}
                    userAuth={loginUserAuth.config}
                />
            </ToastBody>
        </Toast>
    );
}, ({ authValue: prev_auth }, { authValue: next_auth }) => (
    prev_auth.system_restart === next_auth.system_restart
    && prev_auth.system_log === next_auth.system_log
    && prev_auth.account === next_auth.account
    && prev_auth.config === next_auth.config
));

const Permission = React.memo(({ name, checked, changeFunc, userAuth }) => {
    const [tooltipOpen, setTooltipOpen] = useState(false);
    const toggle = () =>  setTooltipOpen(!tooltipOpen);

    return (
        <>
            <input
                type="checkbox"
                id={name}
                name={name}
                value={name}
                checked={checked}
                onChange={userAuth ? changeFunc : () => {}}
                className={!userAuth ? "no-permission" : "permission"}
            />
            <label htmlFor={name} id={`${name}-label`}>
                { !userAuth && <FontAwesomeIcon className={"no-permission-icon"} icon={faTimesCircle} />}
                <div className="auth-name">{permissionMsg(name)}</div>
            </label>
            { !userAuth &&
                <Tooltip placement="top" isOpen={tooltipOpen} target={`${name}-label`} toggle={toggle}>
                    No permission to make changes.
                </Tooltip>
            }
        </>
    );
});