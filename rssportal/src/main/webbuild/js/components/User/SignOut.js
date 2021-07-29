import React, {Component} from 'react';
import {connect} from 'react-redux'
import * as API from "../../api";
import * as Define from "../../define";
import ReactTransitionGroup from "react-addons-css-transition-group";
import UserAuthFrom from "../Form/UserAuthForm";
import InputForm from "../Form/InputForm";
import {PopoverBody, PopoverHeader, UncontrolledPopover} from "reactstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamation, faExclamationCircle} from "@fortawesome/free-solid-svg-icons";
import {bindActionCreators} from "redux";
import * as userActions from "../../modules/User";
import AlertModal from "../Common/AlertModal";
import {initialStateAuth} from "../../modules/login";

class SignOut extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isModalOpen: false,
            uInfo:{
                name:'',
                pwd:'',
                cfpwd:'',
                authValue: {
                    ...initialStateAuth
                }
            },
            errors: {
                name: '',
                pwd: '',
                cfpwd: '',
                ModalMsg: '',
            },
        };
    }

    openModal = () => {
        this.setState(() => ({isModalOpen: true}));
    }
    closeModal = () => {
        this.setState(() => ({isModalOpen: false}));
    }

    SignOutProcess = async (e) => {
        const {uInfo, errors} = this.state;
        const nameRegex = /^([a-zA-Z0-9])([a-zA-Z0-9._-]{1,28})([a-zA-Z0-9]$)/g;
        const pwRegex = /[0-9a-zA-Z]{6,30}$/g;

        if(!nameRegex.test(uInfo.name)) {
            this.setState({
                errors: {
                    name: "Name is invalid.",
                    ...this.initState,
                }
            });
        } else if (!pwRegex.test(uInfo.pwd)) {
            this.setState({
                errors: {
                    pwd: "Password is invalid.",
                    ...this.initState,
                }
            });
        } else if (uInfo.cfpwd.length === 0) {
            this.setState({
                errors: {
                    cfpwd: "Please enter the confirm password.",
                    ...this.initState,
                }
            });
        } else if (uInfo.pwd !== uInfo.cfpwd) {
            this.setState(() => (
                {
                    ...this.state,
                    errors: {
                        pwd: API.getErrorMsg(Define.CHANGE_PW_FAIL_NOT_MATCH_NEW_PASSWORD),
                        ...this.initState,
                    }
                })
            );
        } else {
            let result = 0;
            try {
                await API.createUser(this.props, uInfo);
            } catch (e) {
                console.error(e);
            }
            result = API.getUserInfoErrorCode(this.props);
            console.log("result:" + result);
            if (result !== 0) {
                this.setState(() => (
                    {
                        ...this.state,
                        errors: {
                            pwd: (result !== Define.USER_SET_FAIL_SAME_NAME) ? API.getErrorMsg(result) : "",
                            name: (result === Define.USER_SET_FAIL_SAME_NAME) ? API.getErrorMsg(result) : "",
                            ...this.initState,
                        }
                    })
                );
            } else {
                try {
                    await API.getDBUserList(this.props);
                } catch (e) {
                    console.error(e);
                }
                this.close(); //create modal Close
                this.props.alertOpen("create");
            }
        }
    }

    handlePermission = (e) => {
        const { name } = e.target;
        this.setState((prevState) => ({
            uInfo: {
                ...prevState.uInfo,
                authValue : {
                    ...prevState.uInfo.authValue,
                    [name]: !prevState.uInfo.authValue[name]
                }
            }
        }));
    }

    handleInput = (e) => {
        const { name, value } = e.target;
        let nState = this.state;

        switch (name) {
            case 'name':
                nState.uInfo.name = value;
                break;
            case 'pwd':
                nState.uInfo.pwd = value;
                break;
            case 'cfpwd':
                nState.uInfo.cfpwd = value;
                break;
            default:
                break;
        }

        this.setState({...nState});
    };
    data = {
        titleMsg:'Create Account'
    };

    initState = {
        uInfo : {
            name:'',
            pwd:'',
            cfpwd:'',
            authValue: {
                ...initialStateAuth
            }
        },
        errors: {
            name: '',
            pwd: '',
            cfpwd: '',
            ModalMsg: '',
        },
    };
    close = () => {
        this.setState({
            ...this.initState
        }, () => {
            this.props.right();
        });
    }
    render() {
        const {errors, uInfo} = this.state;
        const {isOpen, right} = this.props;

        // console.log("this.state", this.state);
        // console.log("uInfo", uInfo);

        return (
            <>
                {
                    isOpen ? (
                        <ReactTransitionGroup
                            transitionName={'Custom-modal-anim'}
                            transitionEnterTimeout={200}
                            transitionLeaveTimeout={200}>
                            <div className="Custom-modal-overlay" onClick={this.close} />
                            <div className="Custom-modal">
                                <p className="title">{this.data.titleMsg}</p>
                                <div className="content-with-title user-modal">
                                    <InputForm iType ={"text"}
                                               iName={"name"}
                                               iId="name"
                                               iErrMsg={errors.name}
                                               iLabel={"User Name"}
                                               iPlaceholder ={"Enter the name."}
                                               changeFunc={(e) => this.handleInput(e)}
                                               maxLength={30}
                                    />
                                    <UncontrolledPopover
                                        placement="bottom-end"
                                        target="name"
                                        trigger="hover"
                                        delay={{ show: 300, hide: 0 }}
                                    >
                                        <PopoverHeader>Name</PopoverHeader>
                                        <PopoverBody>
                                            <p>
                                                <FontAwesomeIcon icon={faExclamation} />{" "}
                                                Characters that can be entered: alphabet, number, dot(.), low line(_), hyphen(-).
                                            </p>
                                            <p>
                                                <FontAwesomeIcon icon={faExclamation} />{" "}
                                                Start and end must be entered in alphabet or number.
                                            </p>
                                            <p>
                                                <FontAwesomeIcon icon={faExclamation} />{" "}
                                                Allowed to be at least 3 characters long and up to 30 characters long.
                                            </p>
                                        </PopoverBody>
                                    </UncontrolledPopover>
                                    <InputForm iType ={"password"}
                                               iName={"pwd"}
                                               iId="pwd"
                                               iLabel={"Password"}
                                               iErrMsg={errors.pwd}
                                               iPlaceholder ={"Enter the password."}
                                               changeFunc={(e) => this.handleInput(e)}
                                               maxLength={30}
                                     />
                                    <UncontrolledPopover
                                        placement="top-end"
                                        target="pwd"
                                        trigger="hover"
                                        delay={{ show: 300, hide: 0 }}
                                    >
                                        <PopoverHeader>Password</PopoverHeader>
                                        <PopoverBody>
                                            <p>
                                                <FontAwesomeIcon icon={faExclamation} />{" "}
                                                Characters that can be entered: alphabet, number.
                                            </p>
                                            <p>
                                                <FontAwesomeIcon icon={faExclamation} />{" "}
                                                Allowed to be at least 6 characters long and up to 30 characters long.
                                            </p>
                                        </PopoverBody>
                                    </UncontrolledPopover>
                                    <InputForm iType ={"password"}
                                               iName={"cfpwd"}
                                               iId="cfpwd"
                                               iErrMsg={errors.cfpwd}
                                               iLabel={"Confirm Password"}
                                               changeFunc={(e) => this.handleInput(e)}
                                               iPlaceholder ={"Enter the confirm password."}
                                               maxLength={30}
                                    />
                                    <UserAuthFrom
                                        authValue={uInfo.authValue}
                                        changeFunc={this.handlePermission}
                                        loginUserAuth={this.props.loginInfo.get("auth")}
                                    />
                                </div>
                                <div className="button-wrap no-margin">
                                    <button className="administrator form-type left-btn" onClick={this.SignOutProcess}>
                                        Save
                                    </button>
                                    <button className="administrator form-type right-btn" onClick={this.close}>
                                        Cancel
                                    </button>
                                </div>
                            </div>
                        </ReactTransitionGroup>
                    ) : (
                        <ReactTransitionGroup transitionName={'Custom-modal-anim'} transitionEnterTimeout={200}
                                              transitionLeaveTimeout={200}/>
                    )
                }
                <AlertModal
                    isOpen={this.state.isModalOpen}
                    icon={faExclamationCircle}
                    message={this.state.errors.ModalMsg}
                    style={"administrator"}
                    closer={this.closeModal}
                />
            </>
        );
    }
}

export default connect(
    (state) => ({
        userInfo : state.user.get('UserInfo'),
        loginInfo: state.login.get('loginInfo'),
    }),
    (dispatch) => ({
        userActions: bindActionCreators(userActions, dispatch),
    })
)(SignOut);