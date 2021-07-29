import * as Define from "../define";
import md5 from 'md5';
import {initialStateAuth} from "../modules/login";

export const createUser = (props, uinfo) => {
    const { userActions } = props;

    const requestData = {
        userName: uinfo.name,
        password: md5(uinfo.pwd),
        permission: ObjectToAuthKeyList(uinfo.authValue)
    };

    return userActions.createUser(Define.REST_USERS_POST_CREATE_USER, requestData);
};

export const deleteUser = (props, id) => {
    const { userActions } = props;

    return userActions.deleteUser(`${Define.REST_USERS_POST_DELETE_USER}/${id}`);
};

export const getDBUserList = (props) => {
    const { userActions } = props;
    return userActions.loadUserList(Define.REST_USERS_GET_LIST);
};

export const getUserList = (props) => {
    const { UserList } = props;
    return UserList.toJS().list;
};

export const getUserInfoErrorCode = (props) => {
    const { userInfo } = props;
    return userInfo.toJS().result;
};

export const getUserAuth = (props, id) => {
    const { list } = props.UserList.toJS();
    if(id > 0) {
        const find = list.find(item => item.id === id);
        return find.auth;
    }
    return null;
};

export const changePermission = (props, url, requestData) => {
    const { userActions } = props;
    return userActions.changeUserPermission(url, requestData);
};

export const listToAuthObj = (list) => {
    const objPermission = {
        ...initialStateAuth
    };
    for(const value of list) {
        objPermission[value] = true;
    }
    return objPermission;
}

export const ObjectToAuthKeyList = (map) => {
    return Object.keys(map).filter(key => map[key])
}