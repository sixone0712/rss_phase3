import * as Define from "../define";

export const convertErrMsg = (error) => {
    let msg;
    switch (error) {
        case Define.GENRE_SET_FAIL_NO_ITEM: msg = "Please choose a category."; break;
        case Define.GENRE_SET_FAIL_SAME_NAME: msg = "The genre name is duplicated."; break;
        case Define.GENRE_SET_FAIL_EMPTY_NAME: msg = "Please input genre name."; break;
        case Define.GENRE_SET_FAIL_SEVER_ERROR: msg = "Network connection error."; break;
        case Define.GENRE_SET_FAIL_NOT_SELECT_GENRE: msg = "Please choose a genre."; break;
        case Define.GENRE_SET_FAIL_NOT_EXIST_GENRE: msg = "The selected genre does not exist in the DB. Update the DB."; break;
        case Define.GENRE_SET_FAIL_NEED_UPDATE: msg = "DB has been changed. Update the DB."; break;
        case Define.GENRE_SET_FAIL_INVALID_NAME: msg = "Genre name is invalid. Please re-enter."; break;
        case Define.SEARCH_FAIL_NO_MACHINE_AND_CATEGORY: msg = "Please choose a machine and category."; break;
        case Define.SEARCH_FAIL_NO_MACHINE: msg = "Please choose a machine."; break;
        case Define.SEARCH_FAIL_NO_CATEGORY: msg = "Please choose a category."; break;
        case Define.SEARCH_FAIL_DATE: msg = "Please set the start time before the end time."; break;
        case Define.SEARCH_FAIL_SERVER_ERROR: msg = "Network connection error."; break;
        case Define.FILE_FAIL_NO_ITEM: msg = "Please choose a file."; break;
        case Define.LOGIN_FAIL_NO_REGISTER_USER: msg = "No such user."; break;
        case Define.LOGIN_FAIL_NO_USERNAME_PASSWORD: msg = "Username or Password field is empty"; break;
        case Define.LOGIN_FAIL_INCORRECT_PASSWORD: msg = "Login failed for a invalid password."; break;
        case Define.LOGIN_FAIL_EMPTY_USER_PASSWORD: msg = "Username or Password field is empty"; break;
        case Define.CHANGE_PW_FAIL_EMPTY_PASSWORD: msg = "New Password is incorrect"; break;
        case Define.CHANGE_PW_FAIL_INCORRECT_CURRENT_PASSWORD: msg = "Current Password is incorrect"; break;
        case Define.CHANGE_PW_FAIL_NOT_MATCH_NEW_PASSWORD: msg = "New and Confirm Password is not match"; break;
        case Define.CHANGE_PW_FAIL_CURRENT_NEW_SAME_PASSWORD: msg = "Current password and new password are the same."; break;
        case Define.COMMAND_FAIL_SAME_NAME: msg = "duplicated command."; break;
        case Define.COMMAND_FAIL_EMPTY_NAME: msg = "Please input command."; break;
        case Define.COMMAND_NO_SUCH_COMMAND: msg = "No such command."; break;
        case Define.COMMAND_NO_SELECT_COMMAND: msg = "Please choose a command."; break;
        case Define.COMMAND_EXIST_COMMAND: msg = "Already exist command."; break;
        case Define.USER_SET_FAIL_SAME_NAME: msg = "The your name is duplicated."; break;
        case Define.USER_SET_FAIL_NO_REASON: msg = "Account create failed"; break;
        default: msg="what's error : " + error; break;
    }
    return msg;
};


export const getErrorMsg = (error) => {
    let msg = "";
    switch (error) {
        case Define.GENRE_SET_FAIL_NO_ITEM: msg = "Please choose a category."; break;
        case Define.GENRE_SET_FAIL_SAME_NAME: msg = "The genre name is duplicated."; break;
        case Define.GENRE_SET_FAIL_EMPTY_NAME: msg = "Please input genre name."; break;
        case Define.GENRE_SET_FAIL_SEVER_ERROR: msg = "Network connection error."; break;
        case Define.GENRE_SET_FAIL_NOT_SELECT_GENRE: msg = "Please choose a genre."; break;
        case Define.SEARCH_FAIL_NO_MACHINE_AND_CATEGORY: msg = "Please choose a machine and category."; break;
        case Define.SEARCH_FAIL_NO_MACHINE: msg = "Please choose a machine."; break;
        case Define.SEARCH_FAIL_NO_CATEGORY: msg = "Please choose a category."; break;
        case Define.SEARCH_FAIL_DATE: msg = "Please set the start time before the end time."; break;
        case Define.SEARCH_FAIL_SERVER_ERROR: msg = "Network connection error."; break;
        case Define.SEARCH_FAIL_NO_COMMAND: msg = "Please choose a command."; break;
        case Define.FILE_FAIL_NO_ITEM: msg = "Please choose a file."; break;
        case Define.FILE_FAIL_SERVER_ERROR: msg = "File download failed. After searching again, please download."; break;
        case Define.LOGIN_FAIL_NO_REGISTER_USER: msg = "No such user."; break;
        case Define.LOGIN_FAIL_NO_USERNAME_PASSWORD: msg = "Username or Password field is empty"; break;
        case Define.LOGIN_FAIL_INCORRECT_PASSWORD: msg = "Login failed for a invalid password."; break;
        case Define.LOGIN_FAIL_EMPTY_USER_PASSWORD: msg = "Username or Password field is empty"; break;
        case Define.CHANGE_PW_FAIL_EMPTY_PASSWORD: msg = "New Password is incorrect"; break;
        case Define.CHANGE_PW_FAIL_INCORRECT_CURRENT_PASSWORD: msg = "Current Password is incorrect"; break;
        case Define.CHANGE_PW_FAIL_NOT_MATCH_NEW_PASSWORD: msg = "New and Confirm Password is not match"; break;
        case Define.CHANGE_PW_FAIL_CURRENT_NEW_SAME_PASSWORD: msg = "Current password and new password are the same."; break;
        case Define.DB_UPDATE_ERROR_NO_SUCH_USER: msg = "Update fail. No such user"; break;
        case Define.COMMAND_FAIL_SAME_NAME: msg = "duplicated command."; break;
        case Define.COMMAND_FAIL_EMPTY_NAME: msg = "Please input command."; break;
        case Define.COMMAND_NO_SUCH_COMMAND: msg = "No such command."; break;
        case Define.COMMAND_NO_SELECT_COMMAND: msg = "Please choose a command."; break;
        case Define.COMMAND_EXIST_COMMAND: msg = "Already exist command."; break;
        case Define.USER_SET_FAIL_SAME_NAME: msg = "The your name is duplicated."; break;
        case Define.USER_SET_FAIL_NO_REASON: msg = "Account create failed"; break;

        default: break;
    }

    return msg;
};

export const convertDownloadMsg = (status) => {
    let msg = "";
    switch(status) {
        case 'in-compress':
        case 'done':
            msg = "Compressing...";
            break;
        case 'error':
            msg = 'Error';
            break;
        case 'in-progress':
        default:
            msg = "Downloading...";
            break;
    }
    return msg;
}
