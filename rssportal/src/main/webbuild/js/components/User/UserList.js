import React, {Component} from "react";
import {Breadcrumb, BreadcrumbItem, Button, Card, CardBody, CardHeader, Col, Container, Table} from "reactstrap";
import * as API from "../../api";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as userActions from "../../modules/User";
import {Select} from "antd";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {
    faAngleDoubleUp,
    faCheckCircle,
    faExclamationCircle,
    faTrash,
    faUser
} from "@fortawesome/free-solid-svg-icons";
import FadeLoader from "react-spinners/FadeLoader";
import {filePaginate, RenderPagination} from "../Common/CommonFunction";
import ConfirmModal from "../Common/ConfirmModal";
import moment from "moment";
import ChangeAuthModal from "./ChangeAuth";
import SignOut from "./SignOut";
import ScrollToTop from "react-scroll-up";
import AlertModal from "../Common/AlertModal";
import Footer from "../Common/Footer";
import {Link as RouterLink} from "react-router-dom";
import * as Define from "../../define";

const { Option } = Select;

const AUTH_ALERT_MESSAGE = "Permission change completed.";
const CREATE_ALERT_MESSAGE = "New Account create completed.";
const DELETE_ALERT_MESSAGE = "Account delete completed.";
const DELETE_CONFIRM_MESSAGE ="Do you want to delete account?";

const scrollStyle = {
    backgroundColor: "#343a40",
    width: "40px",
    height: "40px",
    textAlign: "center",
    borderRadius: "3px",
    zIndex: "101",
    bottom: "70px"
};


class UserList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            pageSize: 10,
            currentPage: 1,
            selected: 0,
            isModalOpen : false,
            isAlertOpen: false,
            alertMessage: "",
            isMode:"",
            Permission:"",
            registeredList: null,
            deleteIndex: ""
        };

    }

    async componentDidMount()
    {
        console.log("componentDidMount");
        await this.loadUserList();
    };

    loadUserList = async () => {
        try {
            const res = await API.getDBUserList(this.props);
        } catch (e) {
            console.error(e);
        }
        const lists = API.getUserList(this.props)
        // filter Administrator
        const filterdData = lists.filter(item => item.name !== "Administrator");
        const newData = filterdData.map((item, idx) => {
            return (
                {
                    keyIndex: idx + 1,
                    userAuth: item.auth,
                    userId: item.id,
                    userName: item.name,
                    userCreated: item.created,
                    userLastAccess: item.last_access
                }
            );
        });

        await this.setState({
            ...this.state,
            registeredList: newData
        })

        return true;
    }

    openAlert = async (type) => {
        setTimeout(() => {
            switch(type) {
                case "permission":
                    this.setState({
                        ...this.state,
                        isAlertOpen: true,
                        alertMessage: AUTH_ALERT_MESSAGE
                    });
                    break;
                case "create":
                    this.setState({
                        ...this.state,
                        isAlertOpen: true,
                        alertMessage: CREATE_ALERT_MESSAGE
                    });
                    break;

                case "delete":
                    this.setState({
                        ...this.state,
                        isAlertOpen: true,
                        alertMessage: DELETE_ALERT_MESSAGE
                    });
                    break;


                default:
                    console.log("invalid type!!");
                    break;
            }
        }, 200);

        await this.loadUserList();
    };

    closeAlert = () => {
        this.setState({
            ...this.state,
            isAlertOpen: false,
            alertMessage: ""
        });
    };
    closeModal = async () => {
        await this.setState(() => ({
            ...this.state,
            isMode:'',
            isModalOpen:false,
            selected:0,
        }));
    };
    uDelete = (id, index) => {
        this.setState(() => ({
            ...this.state,
            isMode:'deleteUser',
            isModalOpen:true,
            selected:id,
            deleteIndex: index
        }));
    }
    uChangeAuth = (id) => {
        this.setState(() => ({
            ...this.state,
            isMode:'ChangAuth',
            isModalOpen:true,
            selected:id,
        }));
    };

    handlePaginationChange = page => {
        this.setState({
            ...this.state,
            currentPage: page
        });
    };

    handleSelectBoxChange = value => {
        const { pageSize, currentPage } = this.state;
        const startIndex = (currentPage - 1) * pageSize === 0 ? 1 : (currentPage - 1) * pageSize + 1;

        this.setState({
            pageSize: value,
            currentPage: Math.ceil(startIndex / value)
        });
    };

    DeleteAccount = async () => {
        console.log(this.state.selected);
        try {
            const res = await API.deleteUser(this.props, this.state.selected);
        } catch (e) {
            console.error(e);
        }
        let result = API.getUserInfoErrorCode(this.props);
        console.log("result:" + result);
        if(result !== 0)
        {
            let msg = API.getErrorMsg(result) ;
            if (msg.length > 0) {
                this.props.alertOpen("error");
                this.setState({
                    ...this.state,
                    alertMessage: msg,
                    isAlertOpen: true,
                })
            }
        }
        else
        {
            const { pageSize, deleteIndex } = this.state;
            const numerator = deleteIndex - 1 === 0 ? 1 : deleteIndex - 1;
            await this.closeModal(); //delete modal Close

            this.setState({
                currentPage: Math.ceil(numerator / pageSize),
                deleteIndex: ""
            });

            this.openAlert("delete"); //delete complete
        }
    };

    render() {
        const formatDate = 'YYYY/MM/DD HH:mm:ss';
        const { registeredList, currentPage, pageSize, selected, isModalOpen, isAlertOpen, alertMessage } = this.state;
        const { length: count } = registeredList || 0;
        let renderComponent;
        //console.log(registeredList);

        if (registeredList === null) {
            renderComponent =
                <div className="page-loader-area user-list">
                    <FadeLoader height={30} width={10} radius={2} margin={20} color={"#17a2b8"} />
                </div>;
        } else if (count === 0) {
            renderComponent =
                <Card className="auto-plan-box">
                    <CardHeader className="auto-plan-card-header administrator">
                        User Account
                        <p>Manage <span>user accounts.</span></p>
                    </CardHeader>
                    <CardBody className="auto-plan-card-body no-flex">
                        <Col className="auto-plan-collection-list">
                            <p className="no-registered-plan icon">
                                <FontAwesomeIcon icon={faExclamationCircle} size="7x" />
                            </p>
                            <p className="no-registered-plan message">
                                No registered User List
                            </p>
                        </Col>
                        <div className="user-create-btn">
                            <Button outline size="sm" color="info"
                                    onClick={() => this.setState({...this.state,isModalOpen: true, isMode : "SignOut"})}>
                                <FontAwesomeIcon icon={faUser} /> New Account
                            </Button>
                        </div>
                    </CardBody>
                </Card>;
        } else {
            const users = filePaginate(registeredList, currentPage, pageSize);

            renderComponent =
                <Card className="auto-plan-box administrator">
                    <CardHeader className="auto-plan-card-header administrator">
                        User Account
                        <p>Manage <span>user accounts.</span></p>
                        <div className="select-area">
                            <label>Rows per page : </label>
                            <Select defaultValue={10} onChange={this.handleSelectBoxChange} className="administrator">
                                <Option value={10}>10</Option>
                                <Option value={30}>30</Option>
                                <Option value={50}>50</Option>
                                <Option value={100}>100</Option>
                            </Select>
                        </div>
                    </CardHeader>
                    <CardBody className="auto-plan-card-body not-flex">
                        <div className="user-create-btn">
                            <Button outline size="sm" color="info"
                                    onClick={() => this.setState({...this.state,isModalOpen: true, isMode : "SignOut"})}>
                                <FontAwesomeIcon icon={faUser} /> New Account
                            </Button>
                        </div>
                        <div className="auto-plan-collection-list">
                            <Table>
                                <thead>
                                <tr>
                                    <th>No.</th>
                                    <th>User name</th>
                                    <th>Permission</th>
                                    <th>Account created date</th>
                                    <th>Last access date</th>
                                    <th>Delete</th>
                                </tr>
                                </thead>
                                <tbody>
                                {users.map((user) => {
                                    return (
                                        <tr key={user.keyIndex}>
                                            <td>{user.keyIndex}</td>
                                            <td>{user.userName}</td>
                                            <td>
                                                <div onClick={()=> this.uChangeAuth(user.userId)} className="permission">
                                                    <PermissionList userAuth={user.userAuth}/>
                                                </div>
                                            </td>
                                            <td>{(user.userCreated!=null) ? moment(user.userCreated, "YYYYMMDDHHmmss").format(formatDate): ""}</td>
                                            <td>{(user.userLastAccess!=null) ? moment(user.userLastAccess, "YYYYMMDDHHmmss").format(formatDate): ""}</td>
                                            <td>
                                                <div className="icon-area-administrator" onClick={ () => this.uDelete(user.userId, user.keyIndex) }>
                                                    <FontAwesomeIcon icon={faTrash} />
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </Table>
                        </div>
                    </CardBody>
                    <RenderPagination
                        pageSize={pageSize}
                        itemsCount={count}
                        onPageChange={this.handlePaginationChange}
                        currentPage={currentPage}
                        className={"custom-pagination"}
                    />
                </Card>;
        }

        return (
            <>
                <AlertModal isOpen={isAlertOpen} icon={faCheckCircle} message={alertMessage} style={"administrator"} closer={this.closeAlert} />
                <ConfirmModal isOpen={(isModalOpen && this.state.isMode==='deleteUser')}
                              icon={faTrash}
                              message={DELETE_CONFIRM_MESSAGE}
                              leftBtn={"OK"}
                              rightBtn={"Cancel"}
                              style={"administrator"}
                              actionBg={this.closeModal}
                              actionLeft={this.DeleteAccount}
                              actionRight={this.closeModal}
                />
                <ChangeAuthModal isOpen={isModalOpen && this.state.isMode==='ChangAuth'} right={this.closeModal} alertOpen={this.openAlert} userID={selected} />
                <SignOut isOpen={isModalOpen && this.state.isMode==='SignOut'} right={this.closeModal} alertOpen={this.openAlert}/>
                <Container className="rss-container" fluid={true}>
                    <Breadcrumb className="topic-path">
                        <BreadcrumbItem>
	                        <RouterLink to={Define.PAGE_REFRESH_ADMIN} className="link">
		                        Administrator
	                        </RouterLink>
                        </BreadcrumbItem>
                        <BreadcrumbItem active>User Account</BreadcrumbItem>
                    </Breadcrumb>
                    {renderComponent}
                </Container>
                <Footer/>
                <ScrollToTop showUnder={160} style={scrollStyle}>
                    <span className="scroll-up-icon"><FontAwesomeIcon icon={faAngleDoubleUp} size="lg"/></span>
                </ScrollToTop>
            </>
        );
    }
}

const PermissionList = ({ userAuth }) => {
    return (
        <>
            <div className={"permission-list"}>
                <div className={"enable"}>{Define.PERM_S_MSG_MANUAL_DOWNLOAD_FTP}</div>
                <div className={"divider"}>{"|"}</div>
                <div className={userAuth.manual_vftp ? "enable" : "disable"}>{Define.PERM_S_MSG_MANUAL_DOWNLOAD_VFTP}</div>
                <div className={"divider"}>{"|"}</div>
                <div className={userAuth.auto ? "enable" : "disable"}>{Define.PERM_S_MSG_AUTO_COLLECTION_SETTING}</div>
                <div className={"divider"}>{"|"}</div>
                <div className={userAuth.system_log ? "enable" : "disable"}>{Define.PERM_S_MSG_SYSTEM_LOG_DOWNLOAD}</div>
                <div className={"divider"}>{"|"}</div>
                <div className={userAuth.system_restart ? "enable" : "disable"}>{Define.PERM_S_MSG_SYSTEM_RESTART}</div>
                <div className={"divider"}>{"|"}</div>
                <div className={userAuth.account ? "enable" : "disable"}>{Define.PERM_S_MSG_ACCOUNT_SETTING}</div>
                <div className={"divider"}>{"|"}</div>
                <div className={userAuth.config ? "enable" : "disable"}>{Define.PERM_S_MSG_CONFIG_SETTING}</div>
            </div>
        </>
    )
}

export default connect(
    (state) => ({
        UserList : state.user.get('UserList'),
        userInfo: state.user.get('UserInfo'),
    }),
    (dispatch) => ({
        userActions: bindActionCreators(userActions, dispatch),
    })
)(UserList);