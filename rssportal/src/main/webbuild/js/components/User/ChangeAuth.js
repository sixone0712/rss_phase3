import React, {Component} from "react"
import ReactTransitionGroup from 'react-addons-css-transition-group';
import * as API from "../../api";
import {faExclamationCircle} from "@fortawesome/free-solid-svg-icons";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as loginActions from "../../modules/login";
import UserAuthFrom from "../Form/UserAuthForm";
import * as userActions from "../../modules/User";
import AlertModal from "../Common/AlertModal";
import * as Define from '../../define';
import {ObjectToAuthKeyList} from "../../api";
import {initialStateAuth} from "../../modules/login";

class ChangeAuthModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isModalOpen : false,
            selectedValue: "",
            errors: {
                ModalMsg:''
            },
            authValue: {
                ...initialStateAuth
            },
            selectedId: 0
        };
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        if(nextProps.userID !== 0 && (nextProps.userID !== prevState.selectedId)) {
            const value = API.getUserAuth(nextProps, nextProps.userID) || { ...initialStateAuth };
            console.log("[ChangeAuth]getDerivedStateFromProps get user's permission", nextProps.userID, value);
            return {
                authValue: {
                    ...value
                },
                selectedId: nextProps.userID
            }
        }
        return null;
    }

    changePermissionProcess = async id => {
        const { authValue } = this.state;
        const requestData = {
            permission: ObjectToAuthKeyList(authValue)
        }
        try {
            const res = await API.changePermission(this.props,
              `${Define.REST_USERS_PATCH_CHANGE_PERMISSION}/${id}/permission`, requestData);
        } catch (e) {
            console.error(e);
        }
        const err = API.getUserInfoErrorCode(this.props);
        console.log("changePermission err: ", err);
        if (!err) {
            await API.getDBUserList(this.props);//user list refresh
            this.settingClose();//permission change modal Close & initial
            this.props.alertOpen("permission");
        } else {
            const msg = API.getErrorMsg(err);
            console.log("changePermission msg: ", msg);
            if (msg.length > 0) {
                this.setState({
                    ...this.state,
                    isModalOpen: true,
                    errors: {
                        ...this.state.errors,
                        ModalMsg: msg
                    },
                    authValue: {
                        ...initialStateAuth
                    }
                })
            }
        }
    }

    closeModal = () => {
        this.setState(() => ({...this.state, isModalOpen: false}));
    }

    settingClose = () =>{
        const { right, userID } = this.props;
        const value = API.getUserAuth(this.props, userID) || { ...initialStateAuth };
        this.setState(() => ({
            ...this.state,
            authValue: {
                ...value
            },
            selectedValue: ""

        }));
        right();
    }

    handlePermission = (e) => {
        const { name } = e.target;
        this.setState((prevState) => ({
            ...prevState,
            authValue: {
                ...prevState.authValue,
                [name]: !prevState.authValue[name]
            }
        }));
    }

    data = {
        titleMsg:'Change the Permission'
    };

    render() {
        const { isOpen, right, userID } = this.props;
        const { isModalOpen, errors, authValue } = this.state;

        return (
            <>
                {
                    isOpen ? (
                        <ReactTransitionGroup
                            transitionName={'Custom-modal-anim'}
                            transitionEnterTimeout={200}
                            transitionLeaveTimeout={200} >
                            <div className="Custom-modal-overlay" onClick={this.settingClose} />
                            <div className="Custom-modal">
                                <p className="title">{this.data.titleMsg}</p>
                                <div className="content-with-title user-modal">
                                    <UserAuthFrom
                                        authValue={authValue}
                                        changeFunc={this.handlePermission}
                                        loginUserAuth={this.props.loginInfo.get("auth")}
                                    />
                                </div>
                                <div className="button-wrap no-margin">
                                    <button className="administrator form-type left-btn" onClick={()=>this.changePermissionProcess(userID)}>
                                        Save
                                    </button>
                                    <button className="administrator form-type right-btn" onClick={this.settingClose}>
                                        Cancel
                                    </button>
                                </div>
                            </div>
                        </ReactTransitionGroup>
                    ):(
                        <ReactTransitionGroup transitionName={'Custom-modal-anim'} transitionEnterTimeout={200} transitionLeaveTimeout={200} />
                    )
                }
                <AlertModal isOpen={isModalOpen} icon={faExclamationCircle} message={errors.ModalMsg} style={"administrator"} closer={this.closeModal} />
            </>
        );
    }
}

export default connect(
    (state) => ({
        loginInfo: state.login.get('loginInfo'),
        UserList : state.user.get('UserList'),
        userInfo: state.user.get('UserInfo'),
    }),
    (dispatch) => ({
        loginActions: bindActionCreators(loginActions, dispatch),
        userActions: bindActionCreators(userActions, dispatch),
    })
)(ChangeAuthModal);